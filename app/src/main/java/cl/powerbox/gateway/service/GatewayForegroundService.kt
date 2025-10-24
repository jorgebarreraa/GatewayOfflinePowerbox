package cl.powerbox.gateway.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import cl.powerbox.gateway.http.HttpServer
import cl.powerbox.gateway.util.Logger
import cl.powerbox.gateway.util.NetWatcher
import cl.powerbox.gateway.worker.CleanupWorker
import cl.powerbox.gateway.worker.SyncWorker

class GatewayForegroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "gateway_offline_channel"
        private const val NOTIF_ID = 1
        private const val PREFS = "gateway_prefs"
        private const val KEY_RUNNING = "running"

        const val ACTION_STATE_CHANGED = "cl.powerbox.gateway.STATE_CHANGED"
        const val EXTRA_RUNNING = "running"

        fun start(ctx: Context) {
            val dpCtx = ctx.applicationContext.createDeviceProtectedStorageContext()
            dpCtx.startForegroundService(Intent(dpCtx, GatewayForegroundService::class.java))
        }

        fun stop(ctx: Context) {
            val dpCtx = ctx.applicationContext.createDeviceProtectedStorageContext()
            dpCtx.stopService(Intent(dpCtx, GatewayForegroundService::class.java))
        }

        fun isRunning(ctx: Context): Boolean =
            ctx.applicationContext.createDeviceProtectedStorageContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_RUNNING, false)

        fun queryState(ctx: Context) {
            val dp = ctx.applicationContext.createDeviceProtectedStorageContext()
            val i = Intent(ACTION_STATE_CHANGED).putExtra(EXTRA_RUNNING, isRunning(dp))
            dp.sendBroadcast(i)
        }

        private fun setRunning(ctx: Context, value: Boolean) {
            val dp = ctx.applicationContext.createDeviceProtectedStorageContext()
            dp.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_RUNNING, value).apply()
            dp.sendBroadcast(Intent(ACTION_STATE_CHANGED).putExtra(EXTRA_RUNNING, value))
        }
    }

    private var server: HttpServer? = null
    private var netWatcher: NetWatcher? = null
    private val main = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        val dp = applicationContext.createDeviceProtectedStorageContext()
        Logger.init(dp)
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID, buildNotification("Levantando servidor..."))

        if (server == null) {
            safeStartServerWithRetry()
        } else {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIF_ID, buildNotification("Servidor activo en 127.0.0.1:9090"))
        }

        return START_STICKY
    }

    private fun safeStartServerWithRetry(retries: Int = 5, delayMs: Long = 1500L) {
        try {
            if (server != null) {
                Logger.d("Gateway: server ya estaba activo, refrescando notificación")
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(NOTIF_ID, buildNotification("Servidor activo en 127.0.0.1:9090"))
                return
            }

            Logger.d("Gateway: intentando iniciar HttpServer (retries=$retries)")
            server = HttpServer(applicationContext).also { it.start() }
            setRunning(this, true)

            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(NOTIF_ID, buildNotification("Servidor activo en 127.0.0.1:9090"))

            // Programación
            SyncWorker.schedule(applicationContext)           // 15 min
            SyncWorker.scheduleEvery5Min(applicationContext)  // ticker 5 min
            CleanupWorker.schedule(applicationContext)

            // Net watcher
            netWatcher = NetWatcher(applicationContext) {
                Logger.d("NetWatcher: volvió internet → kick Sync")
                SyncWorker.kick(applicationContext)
            }.also { it.start() }

            Logger.d("Gateway service started OK")
        } catch (t: Throwable) {
            Logger.e("Gateway start failed (retries=$retries): ${t::class.java.simpleName}: ${t.message}", t)

            // Si aún hay reintentos, reintentar en backoff y NO mostrar la notificación de error
            if (retries > 0) {
                main.postDelayed(
                    { safeStartServerWithRetry(retries - 1, (delayMs * 1.7).toLong().coerceAtMost(10_000)) },
                    delayMs
                )
            } else {
                // Sólo mostramos la notificación de error cuando ya no quedan intentos
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(NOTIF_ID, buildNotification("Error iniciando servidor (ver logs)"))
            }
        }
    }

    override fun onDestroy() {
        try { server?.stop() } catch (_: Throwable) {}
        server = null

        try { netWatcher?.stop() } catch (_: Throwable) {}
        netWatcher = null

        setRunning(this, false)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val ch = NotificationChannel(
            CHANNEL_ID,
            "Powerbox Gateway",
            NotificationManager.IMPORTANCE_LOW
        )
        nm.createNotificationChannel(ch)
    }

    private fun buildNotification(text: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Powerbox Gateway")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true)
            .build()
}