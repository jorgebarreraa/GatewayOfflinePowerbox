package cl.powerbox.gateway.http

import android.content.Context
import cl.powerbox.gateway.data.AppDatabase
import cl.powerbox.gateway.data.entity.CachedResponse
import cl.powerbox.gateway.data.entity.PendingRequest
import cl.powerbox.gateway.data.entity.ReplenishmentEvent
import cl.powerbox.gateway.util.Logger
import cl.powerbox.gateway.util.NetworkUtil
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.security.MessageDigest
import java.util.UUID

data class ProxyResult(val status: Int, val contentType: String, val body: ByteArray)

class ProxyHandler(private val ctx: Context) {
    private val db = AppDatabase.get(ctx)
    private val ok = OkHttpClient()
    private val mapper = jacksonObjectMapper()

    private val REAL_BASE = "https://gsvden.coffeeji.com"

    // Endpoints donde aplicamos overlay de stock con localDelta
    private fun isStockListEndpoint(path: String): Boolean {
        return path.contains("coffee/api/device/listTypeAllMaterial") ||
                path.contains("coffee/api/device/replenishList") ||
                path.contains("coffee/api/device/deviceAllInfo") ||
                path.contains("coffee/api/goods/withoutPage")
    }

    // Endpoints de orden críticos que deben responder offline
    private fun isCriticalOrderEndpoint(path: String): Boolean {
        return path.contains("coffee/api/order/genOrder") ||
                path.contains("coffee/api/order/outStockOver") ||
                path.contains("coffee/api/order/produceOver")
    }

    // Reposición
    private fun isReplenishPost(path: String): Boolean {
        val p = path.lowercase()
        return p.contains("replenishsubmit") ||
                p.contains("replenish_add") ||
                p.contains("device/addreplenish") ||
                p.contains("device/submitreplenish") ||
                p.contains("replenishment")
    }

    private fun hashKey(method: String, path: String, body: ByteArray?): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(method.uppercase().toByteArray())
        md.update(path.toByteArray())
        if (body != null) md.update(body)
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    suspend fun handle(
        path: String,
        method: String,
        headers: Headers,
        body: ByteArray?
    ): ProxyResult {
        val online = NetworkUtil.isOnline(ctx)
        val upper = method.uppercase()
        val key = hashKey(upper, path, body)
        val ctJson = "application/json"

        Logger.d("Proxy.handle online=$online method=$upper path=$path body=${body?.size ?: 0}B")

        return if (online) {
            try {
                val allowsBody = upper in listOf("POST", "PUT", "PATCH", "DELETE")
                val reqBody: RequestBody? = if (allowsBody) (body ?: ByteArray(0)).toRequestBody(null) else null

                // POST de reposición: mandamos online y aplicamos local si 2xx
                if (upper == "POST" && isReplenishPost(path)) {
                    val okResp = tryForwardAndReturn(upper, path, headers, reqBody)
                    if (okResp.code in 200..299) {
                        applyReplenishmentLocally(path, body ?: ByteArray(0))
                    }
                    val bytes = okResp.body?.bytes() ?: """{"success":true}""".toByteArray()
                    val ct = okResp.header("Content-Type") ?: ctJson
                    return ProxyResult(okResp.code, ct, bytes)
                }

                val resp = tryForwardAndReturn(upper, path, headers, reqBody)
                val bytes = resp.body?.bytes() ?: ByteArray(0)
                val contentType = resp.header("Content-Type") ?: ctJson

                // Cacheamos JSON
                if (contentType.contains("json", true)) {
                    withContext(Dispatchers.IO) {
                        db.cachedDao().upsert(
                            CachedResponse(
                                key = key,
                                path = path,
                                method = upper,
                                bodyHash = key,
                                contentType = contentType,
                                bytes = bytes
                            )
                        )
                    }
                }

                // Overlay de stock en GET JSON
                if (upper == "GET" && contentType.contains("json", true) && isStockListEndpoint(path)) {
                    val overlay = overlayStockWithLocalDelta(bytes, path)
                    return ProxyResult(resp.code, contentType, overlay)
                }

                ProxyResult(resp.code, contentType, bytes)
            } catch (t: Throwable) {
                Logger.e("Proxy ONLINE ERROR path=$path", t)
                val cached = withContext(Dispatchers.IO) { db.cachedDao().byKey(key) }
                if (cached != null) {
                    val bytes = if (isStockListEndpoint(path) && cached.contentType.contains("json", true))
                        overlayStockWithLocalDelta(cached.bytes, path) else cached.bytes
                    return ProxyResult(200, cached.contentType, bytes)
                }
                return if (isCriticalOrderEndpoint(path)) successCritical(path)
                else ProxyResult(200, ctJson, """{"note":"proxy-error"}""".toByteArray())
            }
        } else {
            // OFFLINE
            if (upper == "POST" && isReplenishPost(path)) {
                applyReplenishmentLocally(path, body ?: ByteArray(0))
                return ProxyResult(200, ctJson, """{"code":200,"success":true}""".toByteArray())
            }

            val cached = withContext(Dispatchers.IO) { db.cachedDao().byKey(key) }
            if (cached != null) {
                val bytes = if (isStockListEndpoint(path) && cached.contentType.contains("json", true))
                    overlayStockWithLocalDelta(cached.bytes, path) else cached.bytes
                return ProxyResult(200, cached.contentType, bytes)
            }

            if (isCriticalOrderEndpoint(path)) {
                enqueuePending(path, upper, headers, body)
                return successCritical(path)
            }

            return ProxyResult(200, ctJson, """{"note":"offline-cached-miss"}""".toByteArray())
        }
    }

    private fun tryForwardAndReturn(method: String, path: String, headers: Headers, reqBody: RequestBody?) =
        ok.newCall(
            Request.Builder()
                .url("$REAL_BASE/" + path.trimStart('/'))
                .method(method, reqBody)
                .headers(headers)
                .build()
        ).execute()

    private suspend fun enqueuePending(
        path: String,
        method: String,
        headers: Headers,
        body: ByteArray?
    ) {
        withContext(Dispatchers.IO) {
            // Guarda un subconjunto útil de cabeceras
            val keep = listOf("Authorization", "Content-Type", "X-Device-Id", "X-Session-Id")
            val hdrMap = mutableMapOf<String, String>()
            for (k in keep) {
                headers[k]?.let { v -> hdrMap[k] = v }
            }
            val headersJson = try { mapper.writeValueAsString(hdrMap) } catch (_: Throwable) { "{}" }

            db.pendingDao().insert(
                PendingRequest(
                    id = UUID.randomUUID().toString(),
                    path = path,
                    method = method,
                    headersJson = headersJson,
                    body = body ?: ByteArray(0),
                    clientOrderId = null,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    private fun overlayStockWithLocalDelta(src: ByteArray, path: String): ByteArray {
        return try {
            val root = mapper.readTree(src)
            val now = System.currentTimeMillis()

            fun applyOnNode(node: JsonNode) {
                val id = when {
                    node.has("productId") -> node.get("productId").asText()
                    node.has("id")        -> node.get("id").asText()
                    else -> null
                } ?: return

                val qtyField = listOf("qty","quantity","stockNum","materialNum","stock","remainNum")
                    .firstOrNull { node.has(it) } ?: return

                val serverQty = node.get(qtyField).asInt(0)
                val state = db.stockStateDao().byId(id)
                val localDelta = state?.localDelta ?: 0
                val effective = (serverQty + localDelta).coerceAtLeast(0)

                if (node is ObjectNode) {
                    node.put(qtyField, effective)
                    node.put("gatewayOverlayTs", now)
                }
            }

            if (root.isArray) root.forEach { applyOnNode(it) }
            else if (root.isObject) {
                if (root.has("materials") && root.get("materials").isArray) {
                    root.get("materials").forEach { applyOnNode(it) }
                } else applyOnNode(root)
            }

            mapper.writeValueAsBytes(root)
        } catch (t: Throwable) {
            Logger.e("overlayStockWithLocalDelta error on $path", t)
            src
        }
    }

    private suspend fun applyReplenishmentLocally(path: String, body: ByteArray) {
        try {
            val node = mapper.readTree(body)
            val now = System.currentTimeMillis()
            val createdAt = node.get("createdAt")?.asLong() ?: now

            val arr = when {
                node.has("items") && node.get("items").isArray -> node.get("items")
                node.has("list") && node.get("list").isArray -> node.get("list")
                node.has("data") && node.get("data").isArray -> node.get("data")
                else -> null
            } ?: return

            val toSave = mutableListOf<ReplenishmentEvent>()
            arr.forEach { it ->
                val id = when {
                    it.has("productId") -> it.get("productId").asText()
                    it.has("id")        -> it.get("id").asText()
                    else -> null
                } ?: return@forEach

                val delta = when {
                    it.has("deltaQty") -> it.get("deltaQty").asInt(0)
                    it.has("qty")      -> it.get("qty").asInt(0)
                    it.has("quantity") -> it.get("quantity").asInt(0)
                    it.has("num")      -> it.get("num").asInt(0)
                    else -> 0
                }
                if (delta == 0) return@forEach

                // Ajusta estado local
                db.stockStateDao().ensureAndDelta(id, delta, createdAt)

                // Encola evento para push
                toSave += ReplenishmentEvent(
                    id = UUID.randomUUID().toString(),
                    productId = id,
                    deltaQty = delta,
                    createdAt = createdAt,
                    sent = false
                )
            }

            if (toSave.isNotEmpty()) {
                withContext(Dispatchers.IO) { db.replenishmentDao().upsertAll(toSave) }
                Logger.d("Proxy APPLY REPL locally: items=${toSave.size} path=$path")
            }
        } catch (t: Throwable) {
            Logger.e("applyReplenishmentLocally parse error path=$path", t)
        }
    }

    private fun successCritical(path: String): ProxyResult {
        val ctJson = "application/json"
        return if (path.contains("genOrder")) {
            val body = """{"code":200,"success":true,"data":{"orderId":"LOCAL-${UUID.randomUUID()}"}}""".toByteArray()
            ProxyResult(200, ctJson, body)
        } else {
            val body = """{"code":200,"success":true,"data":true}""".toByteArray()
            ProxyResult(200, ctJson, body)
        }
    }
}