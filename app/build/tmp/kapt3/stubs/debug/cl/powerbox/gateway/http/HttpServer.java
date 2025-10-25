package cl.powerbox.gateway.http;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u000f\u001a\u00020\u0010J\u0006\u0010\u0011\u001a\u00020\u0010R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0005\u001a\u00020\u00068BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\t\u0010\n\u001a\u0004\b\u0007\u0010\bR\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcl/powerbox/gateway/http/HttpServer;", "", "ctx", "Landroid/content/Context;", "(Landroid/content/Context;)V", "db", "Lcl/powerbox/gateway/data/AppDatabase;", "getDb", "()Lcl/powerbox/gateway/data/AppDatabase;", "db$delegate", "Lkotlin/Lazy;", "engine", "Lio/ktor/server/engine/ApplicationEngine;", "mapper", "Lcom/fasterxml/jackson/databind/ObjectMapper;", "start", "", "stop", "app_debug"})
public final class HttpServer {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context ctx = null;
    @org.jetbrains.annotations.Nullable()
    private io.ktor.server.engine.ApplicationEngine engine;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy db$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final com.fasterxml.jackson.databind.ObjectMapper mapper = null;
    
    public HttpServer(@org.jetbrains.annotations.NotNull()
    android.content.Context ctx) {
        super();
    }
    
    private final cl.powerbox.gateway.data.AppDatabase getDb() {
        return null;
    }
    
    public final void start() {
    }
    
    public final void stop() {
    }
}