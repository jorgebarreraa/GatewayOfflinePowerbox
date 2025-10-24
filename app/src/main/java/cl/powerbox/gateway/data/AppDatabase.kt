package cl.powerbox.gateway.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.powerbox.gateway.data.dao.*
import cl.powerbox.gateway.data.entity.*

@Database(
    entities = [
        CachedResponse::class,
        PendingRequest::class,
        Product::class,
        StockMaster::class,
        SaleEvent::class,
        ReplenishmentEvent::class,
        MachineConfig::class,
        StockState::class // ⬅️ NUEVO: estado de stock efectivo (serverQty + localDelta)
    ],
    version = 4,            // ⬅️ subimos a 4 (agregamos tabla stock_state)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cachedDao(): CachedResponseDao
    abstract fun pendingDao(): PendingRequestDao
    abstract fun productDao(): ProductDao
    abstract fun stockMasterDao(): StockMasterDao
    abstract fun saleDao(): SaleEventDao
    abstract fun replenishmentDao(): ReplenishmentDao
    abstract fun machineConfigDao(): MachineConfigDao
    abstract fun stockStateDao(): StockStateDao // ⬅️ NUEVO

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        // === Migración 1 -> 2: tus tablas nuevas (tal cual las tenías) ===
        private val MIG_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `product` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `price` INTEGER NOT NULL,
                        `recipeJson` TEXT,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `stock_master` (
                        `productId` TEXT NOT NULL,
                        `qty` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`productId`)
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `sale_event` (
                        `id` TEXT NOT NULL,
                        `serverOrderId` TEXT,
                        `clientOrderId` TEXT,
                        `productId` TEXT NOT NULL,
                        `qty` INTEGER NOT NULL,
                        `price` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `sent` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `replenishment_event` (
                        `id` TEXT NOT NULL,
                        `productId` TEXT NOT NULL,
                        `deltaQty` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `sent` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `machine_config` (
                        `key` TEXT NOT NULL,
                        `value` TEXT NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`key`)
                    )
                """.trimIndent())
            }
        }

        // === Migración 2 -> 3: tus columnas nuevas en cached_response (tal cual) ===
        private val MIG_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE cached_response ADD COLUMN cachedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE cached_response ADD COLUMN lastHitAt INTEGER NOT NULL DEFAULT 0")
                val now = System.currentTimeMillis()
                db.execSQL("UPDATE cached_response SET cachedAt = $now, lastHitAt = $now WHERE cachedAt = 0 OR lastHitAt = 0")
            }
        }

        // === Migración 3 -> 4: NUEVA tabla stock_state para stock efectivo local ===
        private val MIG_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `stock_state` (
                        `productId` TEXT NOT NULL,
                        `serverQty` INTEGER NOT NULL,
                        `localDelta` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`productId`)
                    )
                """.trimIndent())
            }
        }

        fun get(ctx: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(ctx.applicationContext, AppDatabase::class.java, "gateway_offline.db")
                    .addMigrations(MIG_1_2, MIG_2_3, MIG_3_4) // ⬅️ mantenemos TODAS, sin borrar datos
                    .build()
                    .also { INSTANCE = it }
            }
    }
}