package cl.powerbox.gateway.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import cl.powerbox.gateway.util.Logger

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val a = intent.action ?: return
        val dpCtx = context.applicationContext.createDeviceProtectedStorageContext()

        Logger.init(dpCtx)
        Logger.d("BootReceiver action=$a")

        val actions = setOf(
            Intent.ACTION_LOCKED_BOOT_COMPLETED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON"
        )
        if (a !in actions) return

        // Arranque inmediato
        try {
            GatewayForegroundService.start(dpCtx)
            Logger.d("BootReceiver -> startForegroundService OK")
        } catch (t: Throwable) {
            Logger.e("BootReceiver start failed", t)
        }

        // Fallback +2s
        try {
            val am = dpCtx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pi = PendingIntent.getService(
                dpCtx, 1,
                Intent(dpCtx, GatewayForegroundService::class.java),
                PendingIntent.FLAG_IMMUTABLE
            )
            am.setExactAndAllowWhileIdle(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 2000L,
                pi
            )
            Logger.d("BootReceiver -> Alarm fallback (+2s)")
        } catch (t: Throwable) {
            Logger.e("BootReceiver alarm failed", t)
        }
    }
}