package cl.powerbox.gateway.data.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0010\bg\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\'J\u000e\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\'J\u000e\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\'J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nH\'J\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\'J\u000e\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\'J\u0016\u0010\u0010\u001a\u00020\b2\u0006\u0010\u0011\u001a\u00020\u0004H\u00a7@\u00a2\u0006\u0002\u0010\u0012J\u0010\u0010\u0013\u001a\u00020\b2\u0006\u0010\u0014\u001a\u00020\nH\'J\u001c\u0010\u0015\u001a\u00020\b2\f\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\n0\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0017J\u0014\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0019J\u0010\u0010\u001a\u001a\u00020\b2\u0006\u0010\u001b\u001a\u00020\u0004H\'J\u0016\u0010\u001c\u001a\u00020\b2\f\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\'\u00a8\u0006\u001e"}, d2 = {"Lcl/powerbox/gateway/data/dao/ReplenishmentEventDao;", "", "all", "", "Lcl/powerbox/gateway/data/entity/ReplenishmentEvent;", "allSent", "allUnsent", "deleteById", "", "id", "", "deleteOldSent", "", "timestamp", "", "getAll", "insert", "e", "(Lcl/powerbox/gateway/data/entity/ReplenishmentEvent;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "markAsSent", "eventId", "markSent", "ids", "(Ljava/util/List;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "pending", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "upsert", "event", "upsertAll", "events", "app_debug"})
@androidx.room.Dao()
public abstract interface ReplenishmentEventDao {
    
    @androidx.room.Query(value = "SELECT * FROM replenishment_events ORDER BY createdAt DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract java.util.List<cl.powerbox.gateway.data.entity.ReplenishmentEvent> all();
    
    @androidx.room.Query(value = "SELECT * FROM replenishment_events ORDER BY createdAt DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract java.util.List<cl.powerbox.gateway.data.entity.ReplenishmentEvent> getAll();
    
    @androidx.room.Query(value = "SELECT * FROM replenishment_events WHERE sent = 0 ORDER BY createdAt ASC")
    @org.jetbrains.annotations.NotNull()
    public abstract java.util.List<cl.powerbox.gateway.data.entity.ReplenishmentEvent> allUnsent();
    
    @androidx.room.Query(value = "SELECT * FROM replenishment_events WHERE sent = 0 ORDER BY createdAt ASC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object pending(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<cl.powerbox.gateway.data.entity.ReplenishmentEvent>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM replenishment_events WHERE sent = 1 ORDER BY createdAt DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract java.util.List<cl.powerbox.gateway.data.entity.ReplenishmentEvent> allSent();
    
    @androidx.room.Insert(onConflict = 1)
    public abstract void upsert(@org.jetbrains.annotations.NotNull()
    cl.powerbox.gateway.data.entity.ReplenishmentEvent event);
    
    @androidx.room.Insert(onConflict = 1)
    public abstract void upsertAll(@org.jetbrains.annotations.NotNull()
    java.util.List<cl.powerbox.gateway.data.entity.ReplenishmentEvent> events);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    cl.powerbox.gateway.data.entity.ReplenishmentEvent e, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "UPDATE replenishment_events SET sent = 1 WHERE id = :eventId")
    public abstract void markAsSent(@org.jetbrains.annotations.NotNull()
    java.lang.String eventId);
    
    @androidx.room.Query(value = "UPDATE replenishment_events SET sent = 1 WHERE id IN (:ids)")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object markSent(@org.jetbrains.annotations.NotNull()
    java.util.List<java.lang.String> ids, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM replenishment_events WHERE sent = 1 AND createdAt < :timestamp")
    public abstract int deleteOldSent(long timestamp);
    
    @androidx.room.Query(value = "DELETE FROM replenishment_events WHERE id = :id")
    public abstract void deleteById(@org.jetbrains.annotations.NotNull()
    java.lang.String id);
}