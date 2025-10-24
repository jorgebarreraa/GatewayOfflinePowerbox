package cl.powerbox.gateway.util

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Logger {
    private const val TAG = "GatewayOffline"
    @Volatile private var file: File? = null
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    fun init(ctx: Context) {
        if (file != null) return
        // Se guarda en /sdcard/Android/data/cl.powerbox.gateway/files/gateway.log
        val dir = ctx.getExternalFilesDir(null) ?: ctx.filesDir
        file = File(dir, "gateway.log")
        write("-- LOGGER INIT --")
    }

    fun d(msg: String) {
        Log.d(TAG, msg)
        write(msg)
    }

    fun e(msg: String, t: Throwable? = null) {
        Log.e(TAG, msg, t)
        write("$msg ${t?.let { "\n" + Log.getStackTraceString(it) } ?: ""}")
    }

    private fun write(text: String) {
        try {
            val f = file ?: return
            f.appendText("${sdf.format(Date())}  $text\n")
        } catch (_: Throwable) { /* ignore */ }
    }
}
