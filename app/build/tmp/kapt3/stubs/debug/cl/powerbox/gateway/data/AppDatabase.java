package cl.powerbox.gateway.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \u00132\u00020\u0001:\u0001\u0013B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&J\b\u0010\u0007\u001a\u00020\bH&J\b\u0010\t\u001a\u00020\nH&J\b\u0010\u000b\u001a\u00020\fH&J\b\u0010\r\u001a\u00020\u000eH&J\b\u0010\u000f\u001a\u00020\u0010H&J\b\u0010\u0011\u001a\u00020\u0012H&\u00a8\u0006\u0014"}, d2 = {"Lcl/powerbox/gateway/data/AppDatabase;", "Landroidx/room/RoomDatabase;", "()V", "cachedDao", "Lcl/powerbox/gateway/data/dao/CachedResponseDao;", "machineConfigDao", "Lcl/powerbox/gateway/data/dao/MachineConfigDao;", "pendingDao", "Lcl/powerbox/gateway/data/dao/PendingRequestDao;", "productDao", "Lcl/powerbox/gateway/data/dao/ProductDao;", "replenishmentDao", "Lcl/powerbox/gateway/data/dao/ReplenishmentDao;", "saleDao", "Lcl/powerbox/gateway/data/dao/SaleEventDao;", "stockMasterDao", "Lcl/powerbox/gateway/data/dao/StockMasterDao;", "stockStateDao", "Lcl/powerbox/gateway/data/dao/StockStateDao;", "Companion", "app_debug"})
@androidx.room.Database(entities = {cl.powerbox.gateway.data.entity.CachedResponse.class, cl.powerbox.gateway.data.entity.PendingRequest.class, cl.powerbox.gateway.data.entity.Product.class, cl.powerbox.gateway.data.entity.StockMaster.class, cl.powerbox.gateway.data.entity.SaleEvent.class, cl.powerbox.gateway.data.entity.ReplenishmentEvent.class, cl.powerbox.gateway.data.entity.MachineConfig.class, cl.powerbox.gateway.data.entity.StockState.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends androidx.room.RoomDatabase {
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile cl.powerbox.gateway.data.AppDatabase INSTANCE;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.room.migration.Migration MIG_1_2 = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.room.migration.Migration MIG_2_3 = null;
    @org.jetbrains.annotations.NotNull()
    private static final androidx.room.migration.Migration MIG_3_4 = null;
    @org.jetbrains.annotations.NotNull()
    public static final cl.powerbox.gateway.data.AppDatabase.Companion Companion = null;
    
    public AppDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract cl.powerbox.gateway.data.dao.CachedResponseDao cachedDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract cl.powerbox.gateway.data.dao.PendingRequestDao pendingDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract cl.powerbox.gateway.data.dao.ProductDao productDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract cl.powerbox.gateway.data.dao.StockMasterDao stockMasterDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract cl.powerbox.gateway.data.dao.SaleEventDao saleDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract cl.powerbox.gateway.data.dao.ReplenishmentDao replenishmentDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract cl.powerbox.gateway.data.dao.MachineConfigDao machineConfigDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract cl.powerbox.gateway.data.dao.StockStateDao stockStateDao();
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\t\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u000bR\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcl/powerbox/gateway/data/AppDatabase$Companion;", "", "()V", "INSTANCE", "Lcl/powerbox/gateway/data/AppDatabase;", "MIG_1_2", "Landroidx/room/migration/Migration;", "MIG_2_3", "MIG_3_4", "get", "ctx", "Landroid/content/Context;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final cl.powerbox.gateway.data.AppDatabase get(@org.jetbrains.annotations.NotNull()
        android.content.Context ctx) {
            return null;
        }
    }
}