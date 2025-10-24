package cl.powerbox.gateway.http;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001e\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00062\u0006\u0010\u0010\u001a\u00020\u0011H\u0082@\u00a2\u0006\u0002\u0010\u0012J0\u0010\u0013\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00062\u0006\u0010\u0014\u001a\u00020\u00062\u0006\u0010\u0015\u001a\u00020\u00162\b\u0010\u0010\u001a\u0004\u0018\u00010\u0011H\u0082@\u00a2\u0006\u0002\u0010\u0017J0\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u000f\u001a\u00020\u00062\u0006\u0010\u0014\u001a\u00020\u00062\u0006\u0010\u0015\u001a\u00020\u00162\b\u0010\u0010\u001a\u0004\u0018\u00010\u0011H\u0086@\u00a2\u0006\u0002\u0010\u0017J\"\u0010\u001a\u001a\u00020\u00062\u0006\u0010\u0014\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\u00062\b\u0010\u0010\u001a\u0004\u0018\u00010\u0011H\u0002J\u0010\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u000f\u001a\u00020\u0006H\u0002J\u0010\u0010\u001d\u001a\u00020\u001c2\u0006\u0010\u000f\u001a\u00020\u0006H\u0002J\u0010\u0010\u001e\u001a\u00020\u001c2\u0006\u0010\u000f\u001a\u00020\u0006H\u0002J\u0018\u0010\u001f\u001a\u00020\u00112\u0006\u0010 \u001a\u00020\u00112\u0006\u0010\u000f\u001a\u00020\u0006H\u0002J\u0010\u0010!\u001a\u00020\u00192\u0006\u0010\u000f\u001a\u00020\u0006H\u0002J*\u0010\"\u001a\u00020#2\u0006\u0010\u0014\u001a\u00020\u00062\u0006\u0010\u000f\u001a\u00020\u00062\u0006\u0010\u0015\u001a\u00020\u00162\b\u0010$\u001a\u0004\u0018\u00010%H\u0002R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006&"}, d2 = {"Lcl/powerbox/gateway/http/ProxyHandler;", "", "ctx", "Landroid/content/Context;", "(Landroid/content/Context;)V", "REAL_BASE", "", "db", "Lcl/powerbox/gateway/data/AppDatabase;", "mapper", "Lcom/fasterxml/jackson/databind/ObjectMapper;", "ok", "Lokhttp3/OkHttpClient;", "applyReplenishmentLocally", "", "path", "body", "", "(Ljava/lang/String;[BLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "enqueuePending", "method", "headers", "Lokhttp3/Headers;", "(Ljava/lang/String;Ljava/lang/String;Lokhttp3/Headers;[BLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "handle", "Lcl/powerbox/gateway/http/ProxyResult;", "hashKey", "isCriticalOrderEndpoint", "", "isReplenishPost", "isStockListEndpoint", "overlayStockWithLocalDelta", "src", "successCritical", "tryForwardAndReturn", "Lokhttp3/Response;", "reqBody", "Lokhttp3/RequestBody;", "app_release"})
public final class ProxyHandler {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context ctx = null;
    @org.jetbrains.annotations.NotNull()
    private final cl.powerbox.gateway.data.AppDatabase db = null;
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient ok = null;
    @org.jetbrains.annotations.NotNull()
    private final com.fasterxml.jackson.databind.ObjectMapper mapper = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String REAL_BASE = "https://gsvden.coffeeji.com";
    
    public ProxyHandler(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        super();
    }
    
    private final boolean isStockListEndpoint(java.lang.String path) {
        return false;
    }
    
    private final boolean isCriticalOrderEndpoint(java.lang.String path) {
        return false;
    }
    
    private final boolean isReplenishPost(java.lang.String path) {
        return false;
    }
    
    private final java.lang.String hashKey(java.lang.String method, java.lang.String path, byte[] body) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object handle(@org.jetbrains.annotations.NotNull()
    java.lang.String path, @org.jetbrains.annotations.NotNull()
    java.lang.String method, @org.jetbrains.annotations.NotNull()
    okhttp3.Headers headers, @org.jetbrains.annotations.Nullable()
    byte[] body, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super cl.powerbox.gateway.http.ProxyResult> $completion) {
        return null;
    }
    
    private final okhttp3.Response tryForwardAndReturn(java.lang.String method, java.lang.String path, okhttp3.Headers headers, okhttp3.RequestBody reqBody) {
        return null;
    }
    
    private final java.lang.Object enqueuePending(java.lang.String path, java.lang.String method, okhttp3.Headers headers, byte[] body, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final byte[] overlayStockWithLocalDelta(byte[] src, java.lang.String path) {
        return null;
    }
    
    private final java.lang.Object applyReplenishmentLocally(java.lang.String path, byte[] body, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final cl.powerbox.gateway.http.ProxyResult successCritical(java.lang.String path) {
        return null;
    }
}