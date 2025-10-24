package cl.powerbox.gateway.data.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0002\b\u0006\bg\u0018\u00002\u00020\u0001J\u0018\u0010\u0002\u001a\u0004\u0018\u00010\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001c\u0010\u0007\u001a\u00020\b2\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u00050\nH\u00a7@\u00a2\u0006\u0002\u0010\u000bJ\u0016\u0010\f\u001a\u00020\b2\u0006\u0010\r\u001a\u00020\u000eH\u00a7@\u00a2\u0006\u0002\u0010\u000fJ\u001c\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00050\n2\u0006\u0010\u0011\u001a\u00020\bH\u00a7@\u00a2\u0006\u0002\u0010\u0012J\u000e\u0010\u0013\u001a\u00020\u000eH\u00a7@\u00a2\u0006\u0002\u0010\u0014J\u001e\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0017\u001a\u00020\u000eH\u00a7@\u00a2\u0006\u0002\u0010\u0018J\u0016\u0010\u0019\u001a\u00020\u00162\u0006\u0010\u001a\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u001b\u00a8\u0006\u001c"}, d2 = {"Lcl/powerbox/gateway/data/dao/CachedResponseDao;", "", "byKey", "Lcl/powerbox/gateway/data/entity/CachedResponse;", "key", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteByKeys", "", "keys", "", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteOlderThan", "olderThanMs", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "lruKeys", "limit", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "totalBytes", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "touch", "", "ts", "(Ljava/lang/String;JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "upsert", "e", "(Lcl/powerbox/gateway/data/entity/CachedResponse;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
@androidx.room.Dao()
public abstract interface CachedResponseDao {
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object upsert(@org.jetbrains.annotations.NotNull()
    cl.powerbox.gateway.data.entity.CachedResponse e, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM cached_response WHERE key = :key LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object byKey(@org.jetbrains.annotations.NotNull()
    java.lang.String key, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super cl.powerbox.gateway.data.entity.CachedResponse> $completion);
    
    @androidx.room.Query(value = "UPDATE cached_response SET lastHitAt = :ts WHERE key = :key")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object touch(@org.jetbrains.annotations.NotNull()
    java.lang.String key, long ts, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM cached_response WHERE cachedAt < :olderThanMs")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteOlderThan(long olderThanMs, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @androidx.room.Query(value = "SELECT key FROM cached_response ORDER BY lastHitAt ASC LIMIT :limit")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object lruKeys(int limit, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<java.lang.String>> $completion);
    
    @androidx.room.Query(value = "DELETE FROM cached_response WHERE key IN (:keys)")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteByKeys(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> keys, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @androidx.room.Query(value = "SELECT IFNULL(SUM(LENGTH(bytes)), 0) FROM cached_response")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object totalBytes(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
}