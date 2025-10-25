package cl.powerbox.gateway.sync;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tJ\u000e\u0010\n\u001a\u00020\u000b2\u0006\u0010\b\u001a\u00020\tJ\u000e\u0010\f\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tJ\u000e\u0010\r\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\tR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Lcl/powerbox/gateway/sync/SyncScheduler;", "", "()V", "PERIODIC_SYNC_WORK_NAME", "", "SYNC_WORK_NAME", "cancelAllSync", "", "context", "Landroid/content/Context;", "hasPendingSync", "", "schedulePeriodicSync", "syncNow", "app_release"})
public final class SyncScheduler {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String SYNC_WORK_NAME = "gateway_sync_work";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PERIODIC_SYNC_WORK_NAME = "gateway_periodic_sync";
    @org.jetbrains.annotations.NotNull()
    public static final cl.powerbox.gateway.sync.SyncScheduler INSTANCE = null;
    
    private SyncScheduler() {
        super();
    }
    
    public final void schedulePeriodicSync(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void syncNow(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void cancelAllSync(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final boolean hasPendingSync(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
}