package cl.powerbox.gateway.data.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\t\bg\u0018\u00002\u00020\u0001J\u000e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003H\'J \u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\'J\u0012\u0010\r\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0007\u001a\u00020\bH\'J \u0010\u000e\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000f\u001a\u00020\fH\u0017J \u0010\u0010\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0011\u001a\u00020\n2\u0006\u0010\u000f\u001a\u00020\fH\u0017J \u0010\u0012\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0011\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\'J\u0010\u0010\u0013\u001a\u00020\u00062\u0006\u0010\u0014\u001a\u00020\u0004H\'\u00a8\u0006\u0015"}, d2 = {"Lcl/powerbox/gateway/data/dao/StockStateDao;", "", "all", "", "Lcl/powerbox/gateway/data/entity/StockState;", "applyDelta", "", "pid", "", "delta", "", "ts", "", "byId", "ensureAndDelta", "now", "ensureAndSetServer", "serverQty", "updateServerQty", "upsert", "state", "app_release"})
@androidx.room.Dao()
public abstract interface StockStateDao {
    
    @androidx.room.Insert(onConflict = 1)
    public abstract void upsert(@org.jetbrains.annotations.NotNull()
    cl.powerbox.gateway.data.entity.StockState state);
    
    @androidx.room.Query(value = "SELECT * FROM stock_state WHERE productId = :pid")
    @org.jetbrains.annotations.Nullable()
    public abstract cl.powerbox.gateway.data.entity.StockState byId(@org.jetbrains.annotations.NotNull()
    java.lang.String pid);
    
    @androidx.room.Query(value = "UPDATE stock_state SET serverQty = :serverQty, updatedAt = :ts WHERE productId = :pid")
    public abstract void updateServerQty(@org.jetbrains.annotations.NotNull()
    java.lang.String pid, int serverQty, long ts);
    
    @androidx.room.Query(value = "UPDATE stock_state SET localDelta = localDelta + :delta, updatedAt = :ts WHERE productId = :pid")
    public abstract void applyDelta(@org.jetbrains.annotations.NotNull()
    java.lang.String pid, int delta, long ts);
    
    @androidx.room.Query(value = "SELECT * FROM stock_state")
    @org.jetbrains.annotations.NotNull()
    public abstract java.util.List<cl.powerbox.gateway.data.entity.StockState> all();
    
    @androidx.room.Transaction()
    public abstract void ensureAndDelta(@org.jetbrains.annotations.NotNull()
    java.lang.String pid, int delta, long now);
    
    @androidx.room.Transaction()
    public abstract void ensureAndSetServer(@org.jetbrains.annotations.NotNull()
    java.lang.String pid, int serverQty, long now);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
        
        @androidx.room.Transaction()
        public static void ensureAndDelta(@org.jetbrains.annotations.NotNull()
        cl.powerbox.gateway.data.dao.StockStateDao $this, @org.jetbrains.annotations.NotNull()
        java.lang.String pid, int delta, long now) {
        }
        
        @androidx.room.Transaction()
        public static void ensureAndSetServer(@org.jetbrains.annotations.NotNull()
        cl.powerbox.gateway.data.dao.StockStateDao $this, @org.jetbrains.annotations.NotNull()
        java.lang.String pid, int serverQty, long now) {
        }
    }
}