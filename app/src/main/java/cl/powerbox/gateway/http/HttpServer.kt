package cl.powerbox.gateway.http

import android.content.Context
import cl.powerbox.gateway.data.AppDatabase
import cl.powerbox.gateway.util.Logger
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.request.receiveChannel
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.*
import io.ktor.utils.io.readRemaining
import io.ktor.utils.io.core.readBytes
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

                // ====== Debug opcional: estado actual del stock ======
                get("/__debug/stock") {
                    val list = withContext(Dispatchers.IO) { db.stockStateDao().all() }
                    val out = list.map { s ->
                        mapOf(
                            "id" to s.productId,
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

                // ========================== Proxy catch-all ==========================
                route("/{...}") {
                    get {
                        handleProxyRequest(call, ctx)
                    }

                    post {
                        handleProxyRequest(call, ctx)
                    }

                    put {
                        handleProxyRequest(call, ctx)
                    }

                    delete {
                        handleProxyRequest(call, ctx)
                    }

                    patch {
                        handleProxyRequest(call, ctx)
                    }

                    head {
                        handleProxyRequest(call, ctx)
                    }

                    options {
                        handleProxyRequest(call, ctx)
                    }
                }
            }
        }.start(wait = false)
        Logger.d("HTTP server started on 127.0.0.1:9090")
    }

    fun stop() {
        try {
            engine?.stop(gracePeriodMillis = 1000, timeoutMillis = 2000)
        } catch (_: Throwable) { /* ignore */ }
        engine = null
        Logger.d("HTTP server stopped")
    }
}

// Función suspend standalone para manejar todas las peticiones del proxy
private suspend fun handleProxyRequest(call: ApplicationCall, ctx: Context) {
    val path = call.request.path().trimStart('/')
    val methodStr = call.request.httpMethod.value

    // Body
    val bodyBytes = call.receiveChannel()
        .readRemaining()
        .readBytes()

    // Headers (Ktor -> OkHttp)
    val hb = Headers.Builder()
    call.request.headers.forEach { name, values ->
        values.forEach { value ->
            hb.add(name, value)
        }
    }

    val res = ProxyHandler(ctx).handle(
        path = path,
        method = methodStr,
        headers = hb.build(),
        body = if (bodyBytes.isNotEmpty()) bodyBytes else null
    )

    call.respondBytes(
        bytes = res.body,
        contentType = ContentType.parse(res.contentType),
        status = HttpStatusCode.fromValue(res.status)
    )
}