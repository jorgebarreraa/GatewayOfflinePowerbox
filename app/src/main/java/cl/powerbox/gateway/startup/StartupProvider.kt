package cl.powerbox.gateway.startup

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.SystemClock
import cl.powerbox.gateway.service.GatewayForegroundService
import cl.powerbox.gateway.util.Logger

/**
 * Se inicializa tan pronto se carga el proceso de la app.
 * Útil para arrancar el servicio antes que cualquier Activity.
 */
class StartupProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        // Usar contexto protegido por dispositivo para que funcione antes de desbloquear
        val app = context?.applicationContext ?: return true
        val dpCtx = app.createDeviceProtectedStorageContext()

        Logger.init(dpCtx)
        Logger.d("StartupProvider.onCreate -> starting service")

        // Arranque inmediato
        try {
            GatewayForegroundService.start(dpCtx)
            Logger.d("StartupProvider -> startForegroundService OK")
        } catch (t: Throwable) {
            Logger.e("StartupProvider immediate start failed", t)
        }

        // Fallback por si tarda el arranque
        try {
            val am = dpCtx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pi = PendingIntent.getService(
                dpCtx, 2,
                Intent(dpCtx, GatewayForegroundService::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
            am.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 1500L,
                pi
            )
            Logger.d("StartupProvider -> Alarm fallback (+1.5s)")
        } catch (t: Throwable) {
            Logger.e("StartupProvider alarm failed", t)
        }

        return true
    }

    // No usamos funciones CRUD del proveedor
    override fun query(u: Uri, p: Array<out String>?, s: String?, a: Array<out String>?, o: String?): Cursor? = null
    override fun getType(u: Uri): String? = null
    override fun insert(u: Uri, v: ContentValues?): Uri? = null
    override fun delete(u: Uri, s: String?, a: Array<out String>?): Int = 0
    override fun update(u: Uri, v: ContentValues?, s: String?, a: Array<out String>?): Int = 0
}
