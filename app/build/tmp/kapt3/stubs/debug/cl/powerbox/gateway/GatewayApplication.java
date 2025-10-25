package cl.powerbox.gateway;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0007\u001a\u00020\bH\u0016J\b\u0010\t\u001a\u00020\bH\u0016J\b\u0010\n\u001a\u00020\bH\u0002J\b\u0010\u000b\u001a\u00020\bH\u0002R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcl/powerbox/gateway/GatewayApplication;", "Landroid/app/Application;", "()V", "httpServer", "Lcl/powerbox/gateway/http/HttpServer;", "networkMonitor", "Lcl/powerbox/gateway/util/NetworkMonitor;", "onCreate", "", "onTerminate", "startHttpServer", "startNetworkMonitoring", "app_debug"})
public final class GatewayApplication extends android.app.Application {
    @org.jetbrains.annotations.Nullable()
    private cl.powerbox.gateway.http.HttpServer httpServer;
    @org.jetbrains.annotations.Nullable()
    private cl.powerbox.gateway.util.NetworkMonitor networkMonitor;
    
    public GatewayApplication() {
        super();
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    private final void startHttpServer() {
    }
    
    private final void startNetworkMonitoring() {
    }
    
    @java.lang.Override()
    public void onTerminate() {
    }
}