package cl.powerbox.gateway.util;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u0003\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\u0004J\u001a\u0010\f\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\u00042\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u000eJ\u000e\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0010\u001a\u00020\u0011J\u0010\u0010\u0012\u001a\u00020\n2\u0006\u0010\u0013\u001a\u00020\u0004H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcl/powerbox/gateway/util/Logger;", "", "()V", "TAG", "", "file", "Ljava/io/File;", "sdf", "Ljava/text/SimpleDateFormat;", "d", "", "msg", "e", "t", "", "init", "ctx", "Landroid/content/Context;", "write", "text", "app_release"})
public final class Logger {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "GatewayOffline";
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile java.io.File file;
    @org.jetbrains.annotations.NotNull()
    private static final java.text.SimpleDateFormat sdf = null;
    @org.jetbrains.annotations.NotNull()
    public static final cl.powerbox.gateway.util.Logger INSTANCE = null;
    
    private Logger() {
        super();
    }
    
    public final void init(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
    }
    
    public final void d(@org.jetbrains.annotations.NotNull()
    java.lang.String msg) {
    }
    
    public final void e(@org.jetbrains.annotations.NotNull()
    java.lang.String msg, @org.jetbrains.annotations.Nullable()
    java.lang.Throwable t) {
    }
    
    private final void write(java.lang.String text) {
    }
}