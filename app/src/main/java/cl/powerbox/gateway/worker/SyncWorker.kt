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

/**
 * ✅ VERSIÓN CORREGIDA - NO DUPLICA STOCK
 *
 * FIX PRINCIPAL: Después de sincronizar replenishments:
 * 1. NO actualiza serverQty automáticamente
 * 2. Marca eventos como sent PRIMERO
 * 3. LUEGO hace PULL del stock real del servidor
 * 4. Reescribe caches con valores efectivos
 */
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
            Logger.d("🔄 SyncWorker: Iniciando sincronización...")

            // 1) Reintento de requests originales
            val batch = db.pendingRequestDao().allPending().take(25)
            var syncedPending = 0
            for (p in batch) {
                try {
                    val req = Request.Builder()
                        .url(Endpoints.BASE + "/" + p.path.trimStart('/'))
                        .method(p.method.uppercase(), p.body.toRequestBody(null))
                        .build()
                    ok.newCall(req).execute().use { resp ->
                        if (resp.isSuccessful) {
                            db.pendingRequestDao().deleteById(p.id)
                            syncedPending++
                            Logger.d("✅ Pending request synced: ${p.path}")
                        }
                    }
                } catch (t: Throwable) {
                    Logger.e("Error retrying pending request", t)
                }
            }

            // 2) PUSH ventas pendientes
            val sales = db.saleDao().pending()
            if (sales.isNotEmpty()) {
                val payload = mapper.writeValueAsBytes(sales.map { saleEvent ->
                    mapOf(
                        "idempotencyKey" to (saleEvent.clientOrderId ?: saleEvent.id),
                        "productId" to saleEvent.productId,
                        "qty" to saleEvent.qty,
                        "price" to saleEvent.price,
                        "createdAt" to saleEvent.createdAt
                    )
                })
                if (postJson(Endpoints.SALES_BATCH, payload)) {
                    db.saleDao().markSent(sales.map { it.id })
                    Logger.d("✅ Synced ${sales.size} sales")
                }
            }

            // 3) ✅ PUSH replenishments - VERSIÓN CORREGIDA
            val reps = db.replenishmentEventDao().allUnsent()
            var syncedReps = 0
            if (reps.isNotEmpty()) {
                val payload = mapper.writeValueAsBytes(reps.map { repEvent ->
                    mapOf(
                        "idempotencyKey" to repEvent.id,
                        "productId" to repEvent.productId,
                        "deltaQty" to repEvent.deltaQty,
                        "createdAt" to repEvent.createdAt
                    )
                })

                if (postJson(Endpoints.REPL_BATCH, payload)) {
                    // ✅ CRÍTICO: SOLO marcar como enviados
                    // NO actualizar serverQty aquí porque puede causar duplicación
                    reps.forEach { event ->
                        db.replenishmentEventDao().markAsSent(event.id)
                    }
                    syncedReps = reps.size

                    Logger.d("✅ Synced $syncedReps replenishment events")
                    Logger.d("⚠️  Esperando PULL del stock real del servidor...")
                }
            }

            // 4) ✅ PULL maestro - ESTO actualiza el serverQty con valores reales
            getJson(Endpoints.PRODUCTS)?.let { saveProducts(it) }

            // ✅ CRÍTICO: Hacer PULL del stock DESPUÉS de enviar replenishments
            // Esto garantiza que serverQty tenga el valor correcto del panel
            getJson(Endpoints.STOCK)?.let {
                saveStockToServerQtyAndCleanupDeltas(it, syncedReps > 0)
            }

            getJson(Endpoints.CONFIG)?.let { saveConfig(it) }

            // 5) ✅ Limpiar eventos enviados antiguos (7 días)
            val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            db.replenishmentEventDao().deleteOldSent(weekAgo)

            Logger.d("✅ SyncWorker: Completado - Pending: $syncedPending, Reps: $syncedReps")

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
        val list = node.mapNotNull { jsonNode ->
            val id = jsonNode.get("id")?.asText() ?: return@mapNotNull null
            val name = jsonNode.get("name")?.asText() ?: "Unnamed"
            val price = jsonNode.get("price")?.asLong() ?: 0L

            val recipeNode = jsonNode.get("recipe") ?: jsonNode.get("recipeJson")
            val recipe = recipeNode?.toString()

            val updatedAt = jsonNode.get("updatedAt")?.asLong() ?: System.currentTimeMillis()
            Product(id, name, price, recipe, updatedAt)
        }
        db.productDao().upsertAll(list)
        Logger.d("SyncWorker: saved products=${list.size}")
    }

    /**
     * ✅ VERSIÓN CORREGIDA:
     * - Actualiza serverQty con el valor REAL del panel
     * - Si había replenishments sincronizados, resetea localDelta a 0
     * - NO hace cálculos, confía en el valor del servidor
     */
    private suspend fun saveStockToServerQtyAndCleanupDeltas(node: JsonNode, hadReplenishments: Boolean) {
        if (!node.isArray) return
        val now = System.currentTimeMillis()

        node.forEach { jsonNode ->
            val pid = jsonNode.get("productId")?.asText() ?: jsonNode.get("id")?.asText() ?: return@forEach
            val serverQty = jsonNode.get("qty")?.asInt() ?: jsonNode.get("quantity")?.asInt() ?: return@forEach

            val existing = db.stockStateDao().byId(pid)
            if (existing == null) {
                // Crear nuevo estado con qty del servidor
                db.stockStateDao().upsert(
                    StockState(
                        productId = pid,
                        serverQty = serverQty,
                        localDelta = 0,
                        lastSync = now
                    )
                )
                Logger.d("📊 NEW stock state: $pid = $serverQty (server)")
            } else {
                // ✅ CRÍTICO: Actualizar serverQty con valor real del panel
                db.stockStateDao().upsert(
                    existing.copy(
                        serverQty = serverQty,
                        localDelta = if (hadReplenishments) 0 else existing.localDelta,
                        lastSync = now
                    )
                )

                val deltaInfo = if (hadReplenishments) " (delta reset)" else " (delta preserved: ${existing.localDelta})"
                Logger.d("📊 UPDATED stock: $pid = $serverQty (server)$deltaInfo")
            }
        }

        if (hadReplenishments) {
            Logger.d("✅ Stock synchronized with server after replenishments")
        }
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
            node.forEach { jsonNode ->
                val key = jsonNode.get("key")?.asText() ?: return@forEach
                val value = jsonNode.get("value")?.toString() ?: "\"\""
                val updatedAt = jsonNode.get("updatedAt")?.asLong() ?: System.currentTimeMillis()
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