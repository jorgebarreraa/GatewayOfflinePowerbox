package cl.powerbox.gateway.service;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0010\t\n\u0002\b\u0002\u0018\u0000 \u001d2\u00020\u0001:\u0001\u001dB\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0002J\b\u0010\r\u001a\u00020\u000eH\u0002J\u0014\u0010\u000f\u001a\u0004\u0018\u00010\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u0012H\u0016J\b\u0010\u0013\u001a\u00020\u000eH\u0016J\b\u0010\u0014\u001a\u00020\u000eH\u0016J\"\u0010\u0015\u001a\u00020\u00162\b\u0010\u0011\u001a\u0004\u0018\u00010\u00122\u0006\u0010\u0017\u001a\u00020\u00162\u0006\u0010\u0018\u001a\u00020\u0016H\u0016J\u001c\u0010\u0019\u001a\u00020\u000e2\b\b\u0002\u0010\u001a\u001a\u00020\u00162\b\b\u0002\u0010\u001b\u001a\u00020\u001cH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001e"}, d2 = {"Lcl/powerbox/gateway/service/GatewayForegroundService;", "Landroid/app/Service;", "()V", "main", "Landroid/os/Handler;", "netWatcher", "Lcl/powerbox/gateway/util/NetWatcher;", "server", "Lcl/powerbox/gateway/http/HttpServer;", "buildNotification", "Landroid/app/Notification;", "text", "", "createChannel", "", "onBind", "Landroid/os/IBinder;", "intent", "Landroid/content/Intent;", "onCreate", "onDestroy", "onStartCommand", "", "flags", "startId", "safeStartServerWithRetry", "retries", "delayMs", "", "Companion", "app_release"})
public final class GatewayForegroundService extends android.app.Service {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String CHANNEL_ID = "gateway_offline_channel";
    private static final int NOTIF_ID = 1;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS = "gateway_prefs";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_RUNNING = "running";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_STATE_CHANGED = "cl.powerbox.gateway.STATE_CHANGED";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_RUNNING = "running";
    @org.jetbrains.annotations.Nullable()
    private cl.powerbox.gateway.http.HttpServer server;
    @org.jetbrains.annotations.Nullable()
    private cl.powerbox.gateway.util.NetWatcher netWatcher;
    @org.jetbrains.annotations.NotNull()
    private final android.os.Handler main = null;
    @org.jetbrains.annotations.NotNull()
    public static final cl.powerbox.gateway.service.GatewayForegroundService.Companion Companion = null;
    
    public GatewayForegroundService() {
        super();
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    private final void safeStartServerWithRetry(int retries, long delayMs) {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public android.os.IBinder onBind(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
        return null;
    }
    
    private final void createChannel() {
    }
    
    private final android.app.Notification buildNotification(java.lang.String text) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u000f\u001a\u00020\u00102\u0006\u0010\r\u001a\u00020\u000eJ\u0018\u0010\u0011\u001a\u00020\u00102\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0012\u001a\u00020\fH\u0002J\u000e\u0010\u0013\u001a\u00020\u00102\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u0014\u001a\u00020\u00102\u0006\u0010\r\u001a\u00020\u000eR\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0015"}, d2 = {"Lcl/powerbox/gateway/service/GatewayForegroundService$Companion;", "", "()V", "ACTION_STATE_CHANGED", "", "CHANNEL_ID", "EXTRA_RUNNING", "KEY_RUNNING", "NOTIF_ID", "", "PREFS", "isRunning", "", "ctx", "Landroid/content/Context;", "queryState", "", "setRunning", "value", "start", "stop", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final void start(@org.jetbrains.annotations.NotNull()
        android.content.Context ctx) {
        }
        
        public final void stop(@org.jetbrains.annotations.NotNull()
        android.content.Context ctx) {
        }
        
        public final boolean isRunning(@org.jetbrains.annotations.NotNull()
        android.content.Context ctx) {
            return false;
        }
        
        public final void queryState(@org.jetbrains.annotations.NotNull()
        android.content.Context ctx) {
        }
        
        private final void setRunning(android.content.Context ctx, boolean value) {
        }
    }
}