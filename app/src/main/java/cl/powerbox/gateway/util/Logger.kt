package cl.powerbox.gateway.util

import android.util.Log

object Logger {
    
    private const val TAG = "Gateway"
    private var enabled = true

    fun d(message: String) {
        if (enabled) {
            Log.d(TAG, message)
        }
    }

    fun i(message: String) {
        if (enabled) {
            Log.i(TAG, message)
        }
    }

    fun w(message: String) {
        if (enabled) {
            Log.w(TAG, message)
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (enabled) {
            if (throwable != null) {
                Log.e(TAG, message, throwable)
            } else {
                Log.e(TAG, message)
            }
        }
    }

    fun setEnabled(enabled: Boolean) {
        Logger.enabled = enabled
    }
}