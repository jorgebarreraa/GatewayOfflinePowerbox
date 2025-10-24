package cl.powerbox.gateway.http

import android.content.Context
import cl.powerbox.gateway.data.AppDatabase
import cl.powerbox.gateway.util.Logger
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.path
import io.ktor.server.request.receiveChannel
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.isEmpty
import io.ktor.utils.io.core.readByte
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers

class HttpServer(private val ctx: Context) {

    private var engine: ApplicationEngine? = null
    private val db by lazy { AppDatabase.get(ctx) }
    private val mapper = jacksonObjectMapper()

    fun start() {
        engine = embeddedServer(factory = Netty, host = "127.0.0.1", port = 9090) {
            routing {

                // ===== Debug opcional: estado del stock =====
                get("/__debug/stock") {
                    val list = withContext(Dispatchers.IO) { db.stockStateDao().all() }
                    val out = list.map { s ->
                        mapOf(
                            "id" to s.productId,              // <- tu entidad usa productId
                            "serverQty" to s.serverQty,
                            "localDelta" to s.localDelta,
                            "effective" to (s.serverQty + s.localDelta)
                        )
                    }
                    call.respondBytes(
                        bytes = mapper.writeValueAsBytes(out),
                        contentType = ContentType.Application.Json,
                        status = HttpStatusCode.OK
                    )
                }

                // ======================== Proxy catch-all ========================
                route("/{...}") {
                    methodProxy(HttpMethod.Get)
                    methodProxy(HttpMethod.Post)
                    methodProxy(HttpMethod.Put)
                    methodProxy(HttpMethod.Delete)
                    methodProxy(HttpMethod.Patch)
                    methodProxy(HttpMethod.Head)
                    methodProxy(HttpMethod.Options)
                }
            }
        }.start(wait = false)
    }

    fun stop() {
        engine?.stop(gracePeriodMillis = 1000, timeoutMillis = 2000)
        engine = null
    }

    // ---- helpers ------------------------------------------------------------

    private fun Route.methodProxy(method: HttpMethod) {
        // Se invoca para cada verbo
        io.ktor.server.routing.handle {
            val path = call.request.path().trimStart('/')
            val methodStr = method.value

            val bodyBytes = call.receiveChannel().toByteArray()

            val hBuilder = Headers.Builder()
            call.request.headers.forEach { k, values ->
                values.forEach { v -> hBuilder.add(k, v) }
            }
            val h = hBuilder.build()

            val res = ProxyHandler(ctx).handle(
                path = path,
                method = methodStr,
                headers = h,
                body = if (bodyBytes.isNotEmpty()) bodyBytes else null
            )

            call.respondBytes(
                bytes = res.body,
                contentType = ContentType.parse(res.contentType),
                status = HttpStatusCode.fromValue(res.status)
            )
        }
    }
}

/** Lee todo el cuerpo a un ByteArray (Ktor 2.x). */
private suspend fun ByteReadChannel.toByteArray(): ByteArray {
    val out = ArrayList<Byte>(1024)
    while (!isClosedForRead) {
        val pkt = readRemaining(8192)
        while (!pkt.isEmpty) out += pkt.readByte()
    }
    return out.toByteArray()
}