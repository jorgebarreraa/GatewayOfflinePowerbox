package cl.powerbox.gateway.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import cl.powerbox.gateway.sync.SyncScheduler
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

/**
 * Monitorea la conectividad REAL a internet (no solo WiFi activo)
 * Hace ping a servidores reales para verificar conectividad
 */
class NetworkMonitor(private val context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var isOnline = false
    private var callback: ConnectivityManager.NetworkCallback? = null
    private var pingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    companion object {
        @Volatile
        private var INSTANCE: NetworkMonitor? = null

        /**
         * Obtiene la instancia singleton del NetworkMonitor
         */
        fun get(context: Context): NetworkMonitor {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkMonitor(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }

        /**
         * Método estático para verificar si hay internet
         * Usado por ProxyHandler para decisiones rápidas
         */
        fun isOnline(): Boolean {
            return INSTANCE?.isCurrentlyOnline() ?: false
        }
    }

    // URLs para verificar conectividad real
    private val connectivityCheckUrls = listOf(
        "https://www.google.com",
        "https://www.cloudflare.com",
        "https://1.1.1.1"
    )

    fun startMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Logger.d("📡 Network interface available - checking real connectivity...")
                checkRealConnectivity()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                val wasOnline = isOnline
                isOnline = false
                Logger.d("🔴 Network interface LOST")

                if (wasOnline) {
                    Logger.d("🔌 Internet connection LOST - entering offline mode")
                    onConnectivityChanged(false)
                }
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)

                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val hasValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

                if (hasInternet && hasValidated) {
                    Logger.d("📡 Network capabilities changed - checking real connectivity...")
                    checkRealConnectivity()
                } else {
                    val wasOnline = isOnline
                    isOnline = false
                    if (wasOnline) {
                        Logger.d("🔴 Network validation lost - entering offline mode")
                        onConnectivityChanged(false)
                    }
                }
            }
        }

        try {
            connectivityManager.registerNetworkCallback(networkRequest, callback!!)
            checkRealConnectivity()
            startPeriodicConnectivityCheck()
            Logger.d("✅ Network monitoring started")
        } catch (e: Exception) {
            Logger.e("Failed to register network callback", e)
        }
    }

    fun stopMonitoring() {
        pingJob?.cancel()
        scope.cancel()

        callback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
                Logger.d("❌ Network monitoring stopped")
            } catch (e: Exception) {
                Logger.e("Failed to unregister network callback", e)
            }
        }
        callback = null
    }

    fun isCurrentlyOnline(): Boolean = isOnline

    private fun checkRealConnectivity() {
        scope.launch {
            val hasConnectivity = pingServers()
            val wasOnline = isOnline
            isOnline = hasConnectivity

            if (hasConnectivity && !wasOnline) {
                Logger.d("🟢 Internet connection RESTORED - triggering sync")
                onConnectivityChanged(true)
            } else if (!hasConnectivity && wasOnline) {
                Logger.d("🔴 Internet connection LOST (no response from servers)")
                onConnectivityChanged(false)
            }
        }
    }

    private suspend fun pingServers(): Boolean = withContext(Dispatchers.IO) {
        for (urlString in connectivityCheckUrls) {
            try {
                val url = URL(urlString)
                val connection = url.openConnection() as HttpURLConnection
                connection.apply {
                    requestMethod = "HEAD"
                    connectTimeout = 3000
                    readTimeout = 3000
                    instanceFollowRedirects = false
                    useCaches = false
                }

                val responseCode = connection.responseCode
                connection.disconnect()

                if (responseCode in 200..399) {
                    Logger.d("✅ Connectivity check OK: $urlString (HTTP $responseCode)")
                    return@withContext true
                }
            } catch (e: Exception) {
                Logger.d("❌ Connectivity check failed: $urlString - ${e.message}")
            }
        }

        Logger.d("❌ All connectivity checks failed - device is OFFLINE")
        return@withContext false
    }

    private fun startPeriodicConnectivityCheck() {
        pingJob?.cancel()
        pingJob = scope.launch {
            while (isActive) {
                delay(30_000)
                Logger.d("⏰ Periodic connectivity check...")
                checkRealConnectivity()
            }
        }
    }

    private fun onConnectivityChanged(isNowOnline: Boolean) {
        if (isNowOnline) {
            SyncScheduler.syncNow(context)
        }
    }
}