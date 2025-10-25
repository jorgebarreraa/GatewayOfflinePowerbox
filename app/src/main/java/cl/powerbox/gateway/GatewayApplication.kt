package cl.powerbox.gateway

import android.app.Application
import cl.powerbox.gateway.http.HttpServer
import cl.powerbox.gateway.sync.SyncScheduler
import cl.powerbox.gateway.util.Logger
import cl.powerbox.gateway.util.NetworkMonitor

class GatewayApplication : Application() {

    private var httpServer: HttpServer? = null
    private var networkMonitor: NetworkMonitor? = null

    override fun onCreate() {
        super.onCreate()

        Logger.d("🚀 GatewayApplication starting...")

        startHttpServer()
        SyncScheduler.schedulePeriodicSync(this)
        startNetworkMonitoring()

        Logger.d("✅ GatewayApplication initialized successfully")
    }

    private fun startHttpServer() {
        try {
            httpServer = HttpServer(this)
            httpServer?.start()
            Logger.d("✅ HTTP Server started on 127.0.0.1:9090")
        } catch (e: Exception) {
            Logger.e("❌ Failed to start HTTP server", e)
        }
    }

    private fun startNetworkMonitoring() {
        try {
            networkMonitor = NetworkMonitor(this)
            networkMonitor?.startMonitoring()
            Logger.d("✅ Network monitoring started")
        } catch (e: Exception) {
            Logger.e("❌ Failed to start network monitoring", e)
        }
    }

    override fun onTerminate() {
        super.onTerminate()

        Logger.d("🛑 GatewayApplication terminating...")

        httpServer?.stop()
        networkMonitor?.stopMonitoring()

        Logger.d("✅ GatewayApplication terminated")
    }
}