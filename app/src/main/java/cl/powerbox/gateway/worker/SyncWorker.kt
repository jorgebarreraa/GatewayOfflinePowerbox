package cl.powerbox.gateway.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import cl.powerbox.gateway.data.AppDatabase
import cl.powerbox.gateway.data.entity.*
import cl.powerbox.gateway.util.Logger
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class SyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    object Endpoints {
        const val BASE = "https://gsvden.coffeeji.com"
        const val PRODUCTS = "/coffee/api/products"
        const val STOCK    = "/coffee/api/stock"
        const val CONFIG   = "/coffee/api/config"
        const val SALES_BATCH = "/coffee/api/sales/batch"
        const val REPL_BATCH  = "/coffee/api/replenishments/batch"
    }

    private val db = AppDatabase.get(applicationContext)
    private val ok = OkHttpClient()
    private val mapper = jacksonObjectMapper()
    private val jsonMT = "application/json; charset=utf-8".toMediaTypeOrNull()

    // ===== Foreground para expedited =====
    override suspend fun getForegroundInfo(): ForegroundInfo {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel(SYNC_CHANNEL_ID, "Powerbox Gateway Sync", NotificationManager.IMPORTANCE_LOW)
            nm.createNotificationChannel(ch)
        }
        val notif = NotificationCompat.Builder(applicationContext, SYNC_CHANNEL_ID)
            .setContentTitle("Powerbox Gateway")
            .setContentText("Sincronizando…")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .build()
        return ForegroundInfo(SYNC_NOTIF_ID, notif)
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 1) Reintento de requests originales
            val batch = db.pendingDao().nextBatch(25)
            for (p in batch) {
                try {
                    val req = Request.Builder()
                        .url(Endpoints.BASE + "/" + p.path.trimStart('/'))
                        .method(p.method.uppercase(), p.body.toRequestBody(null))
                        .build()
                    ok.newCall(req).execute().use { resp ->
                        if (resp.isSuccessful) db.pendingDao().markDone(p.id)
                        else db.pendingDao().markAttempt(p.id, "HTTP ${resp.code}")
                    }
                } catch (t: Throwable) {
                    db.pendingDao().markAttempt(p.id, t.message ?: "error")
                }
            }

            // 2) PUSH ventas pendientes
            val sales = db.saleDao().pending()
            if (sales.isNotEmpty()) {
                val payload = mapper.writeValueAsBytes(sales.map {
                    mapOf(
                        "idempotencyKey" to (it.clientOrderId ?: it.id),
                        "productId" to it.productId,
                        "qty" to it.qty,
                        "price" to it.price,
                        "createdAt" to it.createdAt
                    )
                })
                if (postJson(Endpoints.SALES_BATCH, payload)) {
                    // TODO: cuando me confirmes ProductDao/InventoryManager, aquí neutralizamos deltas locales.
                    // inventory.neutralizeSales(...)
                    db.saleDao().markSent(sales.map { it.id })
                }
            }

            // 3) PUSH replenishments
            val reps = db.replenishmentDao().pending()
            if (reps.isNotEmpty()) {
                val payload = mapper.writeValueAsBytes(reps.map {
                    mapOf(
                        "idempotencyKey" to it.id,
                        "productId" to it.productId,
                        "deltaQty" to it.deltaQty,
                        "createdAt" to it.createdAt
                    )
                })
                if (postJson(Endpoints.REPL_BATCH, payload)) {
                    // Si tienes StockStateDao con ensureAndDelta(pid, delta, now), neutralizamos positivos enviados:
                    val now = System.currentTimeMillis()
                    reps.groupBy { it.productId }
                        .mapValues { entry -> entry.value.sumOf { it.deltaQty } }
                        .forEach { (pid: String, sum: Int) ->
                            // Como localDelta tenía +sum, confirmados => restamos +sum para volver a 0
                            runCatching { db.stockStateDao().ensureAndDelta(pid, -sum, now) }
                        }
                    db.replenishmentDao().markSent(reps.map { it.id })
                }
            }

            // 4) PULL maestro
            getJson(Endpoints.PRODUCTS)?.let { saveProducts(it) }
            getJson(Endpoints.STOCK)?.let { saveStockToServerQty(it) }
            getJson(Endpoints.CONFIG)?.let { saveConfig(it) }

            // Ticker 5 min → reencolar
            if (inputData.getBoolean(KEY_TICK, false)) {
                enqueueTicker(applicationContext)
            }

            Result.success()
        } catch (ce: CancellationException) {
            Logger.d("SyncWorker cancelado. Ignorar.")
            Result.success()
        } catch (t: Throwable) {
            Logger.e("SyncWorker error", t)
            Result.retry()
        }
    }

    private fun url(path: String) = Endpoints.BASE + "/" + path.trimStart('/')

    private fun getJson(path: String): JsonNode? {
        val req = Request.Builder().url(url(path)).get().build()
        ok.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return null
            val bytes = resp.body?.bytes() ?: return null
            // Evita ambigüedad de sobrecargas
            return mapper.readTree(bytes.inputStream())
        }
    }

    private fun postJson(path: String, body: ByteArray): Boolean {
        val req = Request.Builder().url(url(path)).post(body.toRequestBody(jsonMT)).build()
        ok.newCall(req).execute().use { resp -> return resp.isSuccessful }
    }

    // ===== Parsers (ajusta a tu JSON real) =====
    private suspend fun saveProducts(node: JsonNode) {
        if (!node.isArray) return
        val list = node.mapNotNull {
            val id = it.get("id")?.asText() ?: return@mapNotNull null
            val name = it.get("name")?.asText() ?: "Unnamed"
            val price = it.get("price")?.asLong() ?: 0L

            // sin warning por operador Elvis en tipo no-nullable
            val recipeNode = it.get("recipe") ?: it.get("recipeJson")
            val recipe = recipeNode?.toString()

            val updatedAt = it.get("updatedAt")?.asLong() ?: System.currentTimeMillis()
            Product(id, name, price, recipe, updatedAt)
        }
        db.productDao().upsertAll(list)
        Logger.d("SyncWorker: saved products=${list.size}")
    }

    /** Vuelca snapshot del panel a serverQty; deja intacto localDelta. */
    private suspend fun saveStockToServerQty(node: JsonNode) {
        if (!node.isArray) return
        val now = System.currentTimeMillis()
        node.forEach {
            val pid = it.get("productId")?.asText() ?: it.get("id")?.asText() ?: return@forEach
            val qty = it.get("qty")?.asInt() ?: it.get("quantity")?.asInt() ?: return@forEach
            runCatching { db.stockStateDao().ensureAndSetServer(pid, qty, now) }
        }
        Logger.d("SyncWorker: saved server stock snapshot")
    }

    private suspend fun saveConfig(node: JsonNode) {
        val list = mutableListOf<MachineConfig>()
        if (node.isObject) {
            val it = node.fields()
            val now = System.currentTimeMillis()
            while (it.hasNext()) {
                val e = it.next()
                list += MachineConfig(e.key, e.value.toString(), now)
            }
        } else if (node.isArray) {
            node.forEach {
                val key = it.get("key")?.asText() ?: return@forEach
                val value = it.get("value")?.toString() ?: "\"\""
                val updatedAt = it.get("updatedAt")?.asLong() ?: System.currentTimeMillis()
                list += MachineConfig(key, value, updatedAt)
            }
        }
        if (list.isNotEmpty()) {
            db.machineConfigDao().upsertAll(list)
            Logger.d("SyncWorker: saved config=${list.size}")
        }
    }

    companion object {
        private const val UNIQUE_PERIODIC = "gateway_sync_periodic"
        private const val UNIQUE_KICK_ONE = "gateway_sync_now"
        private const val UNIQUE_TICKER   = "gateway_sync_5m"
        private const val KEY_TICK = "tick"

        private const val SYNC_CHANNEL_ID = "gateway_sync_channel"
        private const val SYNC_NOTIF_ID = 2

        fun schedule(ctx: Context) {
            val req = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(ctx)
                .enqueueUniquePeriodicWork(UNIQUE_PERIODIC, ExistingPeriodicWorkPolicy.KEEP, req)
        }

        fun kick(ctx: Context) {
            val req = OneTimeWorkRequestBuilder<SyncWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(ctx)
                .enqueueUniqueWork(UNIQUE_KICK_ONE, ExistingWorkPolicy.KEEP, req)
        }

        fun scheduleEvery5Min(ctx: Context) { enqueueTicker(ctx) }

        internal fun enqueueTicker(ctx: Context) {
            val data = workDataOf(KEY_TICK to true)
            val req = OneTimeWorkRequestBuilder<SyncWorker>()
                .setInputData(data)
                .setInitialDelay(5, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(ctx)
                .enqueueUniqueWork(UNIQUE_TICKER, ExistingWorkPolicy.KEEP, req)
        }
    }
}