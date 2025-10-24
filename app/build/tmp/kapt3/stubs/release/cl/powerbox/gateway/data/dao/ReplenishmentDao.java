package cl.powerbox.gateway.data.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0002\b\u0006\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u001c\u0010\u0007\u001a\u00020\u00032\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\tH\u00a7@\u00a2\u0006\u0002\u0010\u000bJ\u0014\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00050\tH\u00a7@\u00a2\u0006\u0002\u0010\rJ\u001c\u0010\u000e\u001a\u00020\u00032\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00050\tH\u00a7@\u00a2\u0006\u0002\u0010\u000b\u00a8\u0006\u0010"}, d2 = {"Lcl/powerbox/gateway/data/dao/ReplenishmentDao;", "", "insert", "", "e", "Lcl/powerbox/gateway/data/entity/ReplenishmentEvent;", "(Lcl/powerbox/gateway/data/entity/ReplenishmentEvent;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markSent", "ids", "", "", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "pending", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "upsertAll", "events", "app_release"})
@androidx.room.Dao()
public abstract interface ReplenishmentDao {
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object upsertAll(@org.jetbrains.annotations.NotNull()
    java.util.List<cl.powerbox.gateway.data.entity.ReplenishmentEvent> events, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    cl.powerbox.gateway.data.entity.ReplenishmentEvent e, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "\n        SELECT *\n        FROM replenishment_event\n        WHERE sent = 0\n        ORDER BY createdAt ASC\n    ")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object pending(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<cl.powerbox.gateway.data.entity.ReplenishmentEvent>> $completion);
    
    @androidx.room.Query(value = "UPDATE replenishment_event SET sent = 1 WHERE id IN (:ids)")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object markSent(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> ids, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}