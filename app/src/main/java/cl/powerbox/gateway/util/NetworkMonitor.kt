package cl.powerbox.gateway.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import cl.powerbox.gateway.sync.SyncScheduler

class NetworkMonitor(private val context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var isOnline = false
    private var callback: ConnectivityManager.NetworkCallback? = null

    fun startMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()

        callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                val wasOffline = !isOnline
                isOnline = true
                
                if (wasOffline) {
                    Logger.d("🌐 Internet connection RESTORED - triggering sync")
                    SyncScheduler.syncNow(context)
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                isOnline = false
                Logger.d("📵 Internet connection LOST - entering offline mode")
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                
                val wasOffline = !isOnline
                isOnline = hasInternet
                
                if (hasInternet && wasOffline) {
                    Logger.d("🌐 Internet validation confirmed - triggering sync")
                    SyncScheduler.syncNow(context)
                }
            }
        }

        try {
            connectivityManager.registerNetworkCallback(networkRequest, callback!!)
            isOnline = NetworkUtil.isOnline(context)
            Logger.d("✅ Network monitoring started (initial state: ${if (isOnline) "ONLINE" else "OFFLINE"})")
        } catch (e: Exception) {
            Logger.e("Failed to register network callback", e)
        }
    }

    fun stopMonitoring() {
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
}