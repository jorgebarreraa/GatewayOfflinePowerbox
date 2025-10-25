package cl.powerbox.gateway.data.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0006\bg\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\'J\u0012\u0010\u0005\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0006\u001a\u00020\u0007H\'J\u0010\u0010\b\u001a\u00020\t2\u0006\u0010\u0006\u001a\u00020\u0007H\'J \u0010\n\u001a\u00020\t2\u0006\u0010\u000b\u001a\u00020\u00072\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\u0017J\u0010\u0010\u0010\u001a\u00020\t2\u0006\u0010\u0006\u001a\u00020\u0007H\'J\u0018\u0010\u0011\u001a\u00020\t2\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\u0012\u001a\u00020\rH\'J\u0010\u0010\u0013\u001a\u00020\t2\u0006\u0010\u0014\u001a\u00020\u0004H\'\u00a8\u0006\u0015"}, d2 = {"Lcl/powerbox/gateway/data/dao/StockStateDao;", "", "all", "", "Lcl/powerbox/gateway/data/entity/StockState;", "byId", "id", "", "deleteById", "", "ensureAndDelta", "productId", "delta", "", "timestamp", "", "resetLocalDelta", "updateServerQty", "qty", "upsert", "state", "app_debug"})
@androidx.room.Dao()
public abstract interface StockStateDao {
    
    @androidx.room.Query(value = "SELECT * FROM stock_state")
    @org.jetbrains.annotations.NotNull()
    public abstract java.util.List<cl.powerbox.gateway.data.entity.StockState> all();
    
    @androidx.room.Query(value = "SELECT * FROM stock_state WHERE productId = :id LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract cl.powerbox.gateway.data.entity.StockState byId(@org.jetbrains.annotations.NotNull()
    java.lang.String id);
    
    @androidx.room.Insert(onConflict = 1)
    public abstract void upsert(@org.jetbrains.annotations.NotNull()
    cl.powerbox.gateway.data.entity.StockState state);
    
    @androidx.room.Query(value = "UPDATE stock_state SET serverQty = :qty WHERE productId = :id")
    public abstract void updateServerQty(@org.jetbrains.annotations.NotNull()
    java.lang.String id, int qty);
    
    @androidx.room.Query(value = "UPDATE stock_state SET localDelta = 0 WHERE productId = :id")
    public abstract void resetLocalDelta(@org.jetbrains.annotations.NotNull()
    java.lang.String id);
    
    @androidx.room.Query(value = "DELETE FROM stock_state WHERE productId = :id")
    public abstract void deleteById(@org.jetbrains.annotations.NotNull()
    java.lang.String id);
    
    @androidx.room.Transaction()
    public abstract void ensureAndDelta(@org.jetbrains.annotations.NotNull()
    java.lang.String productId, int delta, long timestamp);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
        
        @androidx.room.Transaction()
        public static void ensureAndDelta(@org.jetbrains.annotations.NotNull()
        cl.powerbox.gateway.data.dao.StockStateDao $this, @org.jetbrains.annotations.NotNull()
        java.lang.String productId, int delta, long timestamp) {
        }
    }
}