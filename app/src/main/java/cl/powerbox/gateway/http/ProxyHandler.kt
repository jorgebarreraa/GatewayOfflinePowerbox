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

    private fun isStockListEndpoint(path: String): Boolean =
        path.contains("coffee/api/device/listTypeAllMaterial") ||
                path.contains("coffee/api/device/replenishList") ||
                path.contains("coffee/api/device/deviceAllInfo") ||
                path.contains("coffee/api/goods/withoutPage")

    private fun isCriticalOrderEndpoint(path: String): Boolean =
        path.contains("coffee/api/order/genOrder") ||
                path.contains("coffee/api/order/outStockOver") ||
                path.contains("coffee/api/order/produceOver")

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
            handleOnlineRequest(path, upper, headers, body, key, ctJson)
        } else {
            handleOfflineRequest(path, upper, headers, body, key, ctJson)
        }
    }

    // ==================== MODO ONLINE ====================
    private suspend fun handleOnlineRequest(
        path: String,
        method: String,
        headers: Headers,
        body: ByteArray?,
        key: String,
        ctJson: String
    ): ProxyResult {
        try {
            val allowsBody = method in listOf("POST", "PUT", "PATCH", "DELETE")
            val reqBody: RequestBody? =
                if (allowsBody) (body ?: ByteArray(0)).toRequestBody(null) else null

            // 1. MANEJO DE RELLENOS (REPLENISHMENT)
            if (method == "POST" && isReplenishPost(path)) {
                val okResp = tryForwardAndReturn(method, path, headers, reqBody)
                if (okResp.code in 200..299) {
                    applyReplenishmentLocally(path, body ?: ByteArray(0))
                    // ✅ CRÍTICO: Reescribir TODOS los caches con valores efectivos
                    rewriteAllStockCachesWithEffectiveValues()
                }
                val bytes = okResp.body?.bytes() ?: """{"success":true}""".toByteArray()
                val ct = okResp.header("Content-Type") ?: ctJson
                return ProxyResult(okResp.code, ct, bytes)
            }

            // 2. MANEJO DE ÓRDENES/COMPRAS
            if (method == "POST" && isCriticalOrderEndpoint(path)) {
                val okResp = tryForwardAndReturn(method, path, headers, reqBody)

                if (okResp.code in 200..299) {
                    applyOrderStockDecrementLocally(path, body ?: ByteArray(0))
                    // ✅ CRÍTICO: Reescribir TODOS los caches con valores efectivos
                    rewriteAllStockCachesWithEffectiveValues()
                }

                val bytes = okResp.body?.bytes() ?: """{"success":true}""".toByteArray()
                val ct = okResp.header("Content-Type") ?: ctJson
                return ProxyResult(okResp.code, ct, bytes)
            }

            // 3. PETICIÓN NORMAL
            val resp = tryForwardAndReturn(method, path, headers, reqBody)
            val bytes = resp.body?.bytes() ?: ByteArray(0)
            val contentType = resp.header("Content-Type") ?: ctJson

            if (contentType.contains("json", true)) {
                // ✅ MEJORADO: Guardar respuesta original del servidor
                if (method == "GET" && isStockListEndpoint(path)) {
                    updateServerStockQuantities(bytes)
                }

                // Guardar en cache con valores efectivos
                val bytesToCache = if (method == "GET" && isStockListEndpoint(path)) {
                    applyEffectiveValuesToResponse(bytes)
                } else {
                    bytes
                }

                withContext(Dispatchers.IO) {
                    db.cachedDao().upsert(
                        CachedResponse(
                            key = key,
                            path = path,
                            method = method,
                            bodyHash = key,
                            contentType = contentType,
                            bytes = bytesToCache
                        )
                    )
                }
            }

            // ✅ Siempre devolver con valores efectivos en consultas de stock
            val finalBytes = if (method == "GET" && contentType.contains("json", true) && isStockListEndpoint(path)) {
                applyEffectiveValuesToResponse(bytes)
            } else {
                bytes
            }

            return ProxyResult(resp.code, contentType, finalBytes)

        } catch (t: Throwable) {
            Logger.e("Proxy ONLINE ERROR path=$path", t)

            val cached = withContext(Dispatchers.IO) { db.cachedDao().byKey(key) }
            if (cached != null) {
                // El cache ya tiene valores efectivos, devolverlo tal cual
                return ProxyResult(200, cached.contentType, cached.bytes)
            }

            return if (isCriticalOrderEndpoint(path)) {
                enqueuePending(path, method, headers, body)
                applyOrderStockDecrementLocally(path, body ?: ByteArray(0))
                rewriteAllStockCachesWithEffectiveValues()
                successCritical(path)
            } else {
                ProxyResult(200, ctJson, """{"note":"proxy-error"}""".toByteArray())
            }
        }
    }

    // ==================== MODO OFFLINE ====================
    private suspend fun handleOfflineRequest(
        path: String,
        method: String,
        headers: Headers,
        body: ByteArray?,
        key: String,
        ctJson: String
    ): ProxyResult {
        Logger.d("OFFLINE MODE: path=$path method=$method")

        if (method == "POST" && isReplenishPost(path)) {
            applyReplenishmentLocally(path, body ?: ByteArray(0))
            enqueuePending(path, method, headers, body)
            // ✅ CRÍTICO: Reescribir TODOS los caches inmediatamente
            rewriteAllStockCachesWithEffectiveValues()
            return ProxyResult(200, ctJson, """{"code":200,"success":true}""".toByteArray())
        }

        if (method == "POST" && isCriticalOrderEndpoint(path)) {
            applyOrderStockDecrementLocally(path, body ?: ByteArray(0))
            enqueuePending(path, method, headers, body)
            // ✅ CRÍTICO: Reescribir TODOS los caches inmediatamente
            rewriteAllStockCachesWithEffectiveValues()
            return successCritical(path)
        }

        val cached = withContext(Dispatchers.IO) { db.cachedDao().byKey(key) }
        if (cached != null) {
            // ✅ El cache YA tiene valores efectivos, devolver tal cual
            return ProxyResult(200, cached.contentType, cached.bytes)
        }

        return ProxyResult(200, ctJson, """{"note":"offline-cached-miss"}""".toByteArray())
    }

    private fun tryForwardAndReturn(
        method: String,
        path: String,
        headers: Headers,
        body: RequestBody?
    ): okhttp3.Response {
        val url = REAL_BASE + "/" + path.trimStart('/')
        val req = Request.Builder()
            .url(url)
            .method(method, body)
            .headers(headers)
            .build()
        return ok.newCall(req).execute()
    }

    private suspend fun enqueuePending(
        path: String,
        method: String,
        headers: Headers,
        body: ByteArray?
    ) {
        withContext(Dispatchers.IO) {
            val headersMap = headers.toMap()
            val headersJson = mapper.writeValueAsString(headersMap)
            val clientOrderId = try {
                val node = mapper.readTree(body ?: ByteArray(0))
                node.get("clientOrderId")?.asText() ?: node.get("orderId")?.asText()
            } catch (_: Throwable) {
                null
            }

            db.pendingRequestDao().insert(
                PendingRequest(
                    id = UUID.randomUUID().toString(),
                    path = path,
                    method = method,
                    headersJson = headersJson,
                    body = body ?: ByteArray(0),
                    clientOrderId = clientOrderId,
                    createdAt = System.currentTimeMillis()
                )
            )
            Logger.d("ENQUEUED pending request: path=$path orderId=$clientOrderId")
        }
    }

    private suspend fun updateServerStockQuantities(responseBytes: ByteArray) {
        try {
            val root = mapper.readTree(responseBytes)

            suspend fun processNode(node: JsonNode) {
                val id = when {
                    node.has("productId") -> node.get("productId").asText()
                    node.has("id") -> node.get("id").asText()
                    node.has("materialId") -> node.get("materialId").asText()
                    else -> null
                } ?: return

                val qtyField = listOf("qty", "quantity", "stockNum", "materialNum", "stock", "remainNum")
                    .firstOrNull { node.has(it) } ?: return

                val serverQty = node.get(qtyField).asInt(0)

                withContext(Dispatchers.IO) {
                    val existing = db.stockStateDao().byId(id)
                    if (existing == null) {
                        // Crear nuevo estado con serverQty del panel
                        db.stockStateDao().upsert(
                            cl.powerbox.gateway.data.entity.StockState(
                                productId = id,
                                serverQty = serverQty,
                                localDelta = 0,
                                lastSync = System.currentTimeMillis()
                            )
                        )
                    } else {
                        // Solo actualizar serverQty, mantener localDelta
                        db.stockStateDao().updateServerQty(id, serverQty)
                    }
                }
            }

            if (root.isArray) {
                root.forEach { processNode(it) }
            } else if (root.isObject) {
                if (root.has("materials") && root.get("materials").isArray) {
                    root.get("materials").forEach { processNode(it) }
                } else if (root.has("data")) {
                    val data = root.get("data")
                    if (data.isArray) {
                        data.forEach { processNode(it) }
                    } else if (data.isObject) {
                        processNode(data)
                    }
                } else {
                    processNode(root)
                }
            }

            Logger.d("✅ Updated server stock quantities from response")
        } catch (t: Throwable) {
            Logger.e("Error updating server stock quantities", t)
        }
    }

    // ✅ NUEVA FUNCIÓN CRÍTICA: Reescribe el cache con valores efectivos (serverQty + localDelta)
    private suspend fun rewriteAllStockCachesWithEffectiveValues() {
        try {
            withContext(Dispatchers.IO) {
                val stockEndpoints = listOf(
                    "coffee/api/device/listTypeAllMaterial",
                    "coffee/api/device/deviceAllInfo",
                    "coffee/api/device/replenishList",
                    "coffee/api/goods/withoutPage"
                )

                stockEndpoints.forEach { endpoint ->
                    val key = hashKey("GET", endpoint, null)
                    val cached = db.cachedDao().byKey(key)

                    if (cached != null && cached.contentType.contains("json", true)) {
                        // ✅ CRÍTICO: Aplicar valores efectivos y REESCRIBIR el cache
                        val updatedBytes = applyEffectiveValuesToResponse(cached.bytes)
                        db.cachedDao().upsert(cached.copy(bytes = updatedBytes))
                        Logger.d("✅ REWRITTEN cache for: $endpoint")
                    }
                }
            }
        } catch (t: Throwable) {
            Logger.e("Error rewriting stock caches", t)
        }
    }

    // ✅ FUNCIÓN CRÍTICA: Aplica valores efectivos (serverQty + localDelta) al JSON
    private fun applyEffectiveValuesToResponse(src: ByteArray): ByteArray {
        return try {
            val root = mapper.readTree(src)
            val now = System.currentTimeMillis()

            fun applyOnNode(node: JsonNode) {
                val id = when {
                    node.has("productId") -> node.get("productId").asText()
                    node.has("id") -> node.get("id").asText()
                    node.has("materialId") -> node.get("materialId").asText()
                    else -> null
                } ?: return

                val qtyField = listOf("qty", "quantity", "stockNum", "materialNum", "stock", "remainNum")
                    .firstOrNull { node.has(it) } ?: return

                // ✅ Buscar el estado y calcular efectivo
                val state = db.stockStateDao().byId(id)
                val effectiveQty = if (state != null) {
                    (state.serverQty + state.localDelta).coerceAtLeast(0)
                } else {
                    node.get(qtyField).asInt(0)
                }

                if (node is ObjectNode) {
                    node.put(qtyField, effectiveQty)
                    node.put("gatewayTs", now)
                    if (state != null && state.localDelta != 0) {
                        node.put("localDelta", state.localDelta)
                    }
                }
            }

            if (root.isArray) {
                root.forEach { applyOnNode(it) }
            } else if (root.isObject) {
                when {
                    root.has("materials") && root.get("materials").isArray ->
                        root.get("materials").forEach { applyOnNode(it) }
                    root.has("data") && root.get("data").isObject ->
                        applyOnNode(root.get("data"))
                    root.has("data") && root.get("data").isArray ->
                        root.get("data").forEach { applyOnNode(it) }
                    else -> applyOnNode(root)
                }
            }

            mapper.writeValueAsBytes(root)
        } catch (t: Throwable) {
            Logger.e("Error applying effective values", t)
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
                node.isArray -> node
                else -> null
            } ?: return

            val toSave = mutableListOf<ReplenishmentEvent>()

            arr.forEach { item ->
                val id = when {
                    item.has("productId") -> item.get("productId").asText()
                    item.has("id") -> item.get("id").asText()
                    item.has("materialId") -> item.get("materialId").asText()
                    else -> null
                } ?: return@forEach

                val delta = when {
                    item.has("deltaQty") -> item.get("deltaQty").asInt(0)
                    item.has("qty") -> item.get("qty").asInt(0)
                    item.has("quantity") -> item.get("quantity").asInt(0)
                    item.has("num") -> item.get("num").asInt(0)
                    item.has("addNum") -> item.get("addNum").asInt(0)
                    else -> 0
                }

                if (delta == 0) return@forEach

                withContext(Dispatchers.IO) {
                    db.stockStateDao().ensureAndDelta(id, delta, createdAt)
                }

                toSave += ReplenishmentEvent(
                    id = UUID.randomUUID().toString(),
                    productId = id,
                    deltaQty = delta,
                    createdAt = createdAt,
                    sent = false
                )
            }

            if (toSave.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    db.replenishmentEventDao().upsertAll(toSave)
                }
                Logger.d("✅ REPLENISHMENT APPLIED: ${toSave.size} items, path=$path")
            }
        } catch (t: Throwable) {
            Logger.e("applyReplenishmentLocally parse error path=$path", t)
        }
    }

    private suspend fun applyOrderStockDecrementLocally(path: String, body: ByteArray) {
        try {
            val node = mapper.readTree(body)
            val now = System.currentTimeMillis()

            val products = when {
                node.has("products") && node.get("products").isArray -> node.get("products")
                node.has("items") && node.get("items").isArray -> node.get("items")
                node.has("materials") && node.get("materials").isArray -> node.get("materials")
                node.has("goodsList") && node.get("goodsList").isArray -> node.get("goodsList")
                else -> null
            }

            products?.forEach { item ->
                val productId = when {
                    item.has("productId") -> item.get("productId").asText()
                    item.has("materialId") -> item.get("materialId").asText()
                    item.has("goodsId") -> item.get("goodsId").asText()
                    item.has("id") -> item.get("id").asText()
                    else -> null
                } ?: return@forEach

                val quantity = when {
                    item.has("quantity") -> item.get("quantity").asInt(1)
                    item.has("num") -> item.get("num").asInt(1)
                    item.has("count") -> item.get("count").asInt(1)
                    else -> 1
                }

                val delta = -quantity
                withContext(Dispatchers.IO) {
                    db.stockStateDao().ensureAndDelta(productId, delta, now)
                }

                Logger.d("📉 STOCK DECREASED: productId=$productId, delta=$delta")
            }

        } catch (t: Throwable) {
            Logger.e("applyOrderStockDecrementLocally error path=$path", t)
        }
    }

    private fun successCritical(path: String): ProxyResult {
        val ctJson = "application/json"
        return if (path.contains("genOrder")) {
            val orderId = "LOCAL-${UUID.randomUUID()}"
            val body = """{"code":200,"success":true,"data":{"orderId":"$orderId"}}""".toByteArray()
            ProxyResult(200, ctJson, body)
        } else {
            val body = """{"code":200,"success":true,"data":true}""".toByteArray()
            ProxyResult(200, ctJson, body)
        }
    }
}