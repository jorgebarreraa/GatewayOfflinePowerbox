package cl.powerbox.gateway.util;

/**
 * Monitorea la conectividad REAL a internet (no solo WiFi activo)
 * Hace ping a servidores reales para verificar conectividad
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\n\u0018\u0000 \u001c2\u00020\u0001:\u0001\u001cB\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0012\u001a\u00020\u0013H\u0002J\u0006\u0010\u0014\u001a\u00020\rJ\u0010\u0010\u0015\u001a\u00020\u00132\u0006\u0010\u0016\u001a\u00020\rH\u0002J\u000e\u0010\u0017\u001a\u00020\rH\u0082@\u00a2\u0006\u0002\u0010\u0018J\u0006\u0010\u0019\u001a\u00020\u0013J\b\u0010\u001a\u001a\u00020\u0013H\u0002J\u0006\u0010\u001b\u001a\u00020\u0013R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001d"}, d2 = {"Lcl/powerbox/gateway/util/NetworkMonitor;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "callback", "Landroid/net/ConnectivityManager$NetworkCallback;", "connectivityCheckUrls", "", "", "connectivityManager", "Landroid/net/ConnectivityManager;", "isOnline", "", "pingJob", "Lkotlinx/coroutines/Job;", "scope", "Lkotlinx/coroutines/CoroutineScope;", "checkRealConnectivity", "", "isCurrentlyOnline", "onConnectivityChanged", "isNowOnline", "pingServers", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "startMonitoring", "startPeriodicConnectivityCheck", "stopMonitoring", "Companion", "app_release"})
public final class NetworkMonitor {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final android.net.ConnectivityManager connectivityManager = null;
    private boolean isOnline = false;
    @org.jetbrains.annotations.Nullable()
    private android.net.ConnectivityManager.NetworkCallback callback;
    @org.jetbrains.annotations.Nullable()
    private kotlinx.coroutines.Job pingJob;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope scope = null;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile cl.powerbox.gateway.util.NetworkMonitor INSTANCE;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<java.lang.String> connectivityCheckUrls = null;
    @org.jetbrains.annotations.NotNull()
    public static final cl.powerbox.gateway.util.NetworkMonitor.Companion Companion = null;
    
    public NetworkMonitor(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    public final void startMonitoring() {
    }
    
    public final void stopMonitoring() {
    }
    
    public final boolean isCurrentlyOnline() {
        return false;
    }
    
    private final void checkRealConnectivity() {
    }
    
    private final java.lang.Object pingServers(kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    private final void startPeriodicConnectivityCheck() {
    }
    
    private final void onConnectivityChanged(boolean isNowOnline) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00042\u0006\u0010\u0006\u001a\u00020\u0007J\u0006\u0010\b\u001a\u00020\tR\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcl/powerbox/gateway/util/NetworkMonitor$Companion;", "", "()V", "INSTANCE", "Lcl/powerbox/gateway/util/NetworkMonitor;", "get", "context", "Landroid/content/Context;", "isOnline", "", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        /**
         * Obtiene la instancia singleton del NetworkMonitor
         */
        @org.jetbrains.annotations.NotNull()
        public final cl.powerbox.gateway.util.NetworkMonitor get(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
        
        /**
         * Método estático para verificar si hay internet
         * Usado por ProxyHandler para decisiones rápidas
         */
        public final boolean isOnline() {
            return false;
        }
    }
}