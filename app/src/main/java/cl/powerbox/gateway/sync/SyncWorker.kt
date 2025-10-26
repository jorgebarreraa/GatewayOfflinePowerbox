package cl.powerbox.gateway.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import cl.powerbox.gateway.data.AppDatabase
import cl.powerbox.gateway.util.Logger
import cl.powerbox.gateway.util.NetworkUtil
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val db = AppDatabase.get(context)
    private val mapper = jacksonObjectMapper()
    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val REAL_BASE = "https://gsvden.coffeeji.com"

    override suspend fun doWork(): Result {
        return try {
            if (!NetworkUtil.isOnline(applicationContext)) {
                Logger.d("SyncWorker: No internet connection, will retry later")
                return Result.retry()
            }

            Logger.d("🔄 SyncWorker: Starting synchronization...")

            val syncedPending = syncPendingRequests()
            val syncedReplenishments = syncReplenishmentEvents()
            cleanupSyncedStockStates()

            Logger.d("✅ SyncWorker: Completed - Pending: $syncedPending, Replenishments: $syncedReplenishments")

            Result.success()
        } catch (e: Exception) {
            Logger.e("❌ SyncWorker: Error during sync", e)
            Result.retry()
        }
    }

    private suspend fun syncPendingRequests(): Int {
        val pending = withContext(Dispatchers.IO) {
            db.pendingRequestDao().allPending()
        }

        if (pending.isEmpty()) {
            Logger.d("No pending requests to sync")
            return 0
        }

        Logger.d("Syncing ${pending.size} pending requests...")
        var synced = 0

        for (req in pending) {
            try {
                @Suppress("UNCHECKED_CAST")
                val headersMap = try {
                    mapper.readValue(req.headersJson, Map::class.java) as Map<String, String>
                } catch (_: Throwable) {
                    emptyMap<String, String>()
                }

                val headersBuilder = Headers.Builder()
                headersMap.forEach { (k: String, v: String) ->
                    headersBuilder.add(k, v)
                }

                val requestBody = if (req.body.isNotEmpty()) {
                    req.body.toRequestBody(null)
                } else null

                val request = Request.Builder()
                    .url("$REAL_BASE/${req.path.trimStart('/')}")
                    .method(req.method, requestBody)
                    .headers(headersBuilder.build())
                    .build()

                val response = okHttp.newCall(request).execute()

                if (response.code in 200..299) {
                    withContext(Dispatchers.IO) {
                        db.pendingRequestDao().deleteById(req.id)
                    }
                    synced++
                    Logger.d("✅ Synced pending request: ${req.path} (code: ${response.code})")
                } else {
                    Logger.e("⚠️ Failed to sync pending request: ${req.path} (code: ${response.code})")
                }

                response.close()

            } catch (e: Exception) {
                Logger.e("Error syncing pending request: ${req.path}", e)
            }
        }

        return synced
    }

    private suspend fun syncReplenishmentEvents(): Int {
        val events = withContext(Dispatchers.IO) {
            db.replenishmentEventDao().allUnsent()
        }

        if (events.isEmpty()) {
            Logger.d("No replenishment events to sync")
            return 0
        }

        Logger.d("Syncing ${events.size} replenishment events...")
        var synced = 0

        val grouped = events.groupBy { it.productId }

        for ((productId: String, productEvents) in grouped) {
            try {
                val payload = mapOf(
                    "productId" to productId,
                    "events" to productEvents.map { event ->
                        mapOf(
                            "id" to event.id,
                            "deltaQty" to event.deltaQty,
                            "createdAt" to event.createdAt
                        )
                    }
                )

                val jsonBody = mapper.writeValueAsBytes(payload)
                val requestBody = jsonBody.toRequestBody(null)

                val request = Request.Builder()
                    .url("$REAL_BASE/coffee/api/device/syncReplenishments")
                    .post(requestBody)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = okHttp.newCall(request).execute()

                if (response.code in 200..299) {
                    withContext(Dispatchers.IO) {
                        productEvents.forEach { event ->
                            db.replenishmentEventDao().markAsSent(event.id)
                        }
                    }
                    synced += productEvents.size
                    Logger.d("✅ Synced ${productEvents.size} replenishment events for product: $productId")
                } else {
                    Logger.e("⚠️ Failed to sync replenishments for $productId (code: ${response.code})")
                }

                response.close()

            } catch (e: Exception) {
                Logger.e("Error syncing replenishment events for $productId", e)
            }
        }

        return synced
    }

    private suspend fun cleanupSyncedStockStates() {
        withContext(Dispatchers.IO) {
            val allSentEvents = db.replenishmentEventDao().allSent()

            if (allSentEvents.isEmpty()) return@withContext

            // ✅ CRÍTICO: Actualizar serverQty y resetear localDelta
            val syncedDeltas = allSentEvents
                .groupBy { it.productId }
                .mapValues { (_, events) ->
                    events.sumOf { it.deltaQty }
                }

            syncedDeltas.forEach { (productId: String, syncedDelta: Int) ->
                val state = db.stockStateDao().byId(productId)
                if (state != null) {
                    // ✅ NUEVO ENFOQUE: Actualizar serverQty con el valor efectivo
                    val newServerQty = state.serverQty + syncedDelta
                    db.stockStateDao().updateServerQty(productId, newServerQty)

                    // ✅ Resetear localDelta a 0 porque ya se sincronizó
                    db.stockStateDao().resetLocalDelta(productId)

                    Logger.d("✅ Synced product $productId: serverQty = $newServerQty, localDelta = 0")
                }
            }

            val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            db.replenishmentEventDao().deleteOldSent(weekAgo)
        }
    }
}