package cl.powerbox.gateway.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import cl.powerbox.gateway.data.entity.StockState

@Dao
interface StockStateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(state: StockState)

    @Query("SELECT * FROM stock_state WHERE productId = :pid")
    fun byId(pid: String): StockState?

    @Query("UPDATE stock_state SET serverQty = :serverQty, updatedAt = :ts WHERE productId = :pid")
    fun updateServerQty(pid: String, serverQty: Int, ts: Long)

    @Query("UPDATE stock_state SET localDelta = localDelta + :delta, updatedAt = :ts WHERE productId = :pid")
    fun applyDelta(pid: String, delta: Int, ts: Long)

    @Query("SELECT * FROM stock_state")
    fun all(): List<StockState>

    @Transaction
    fun ensureAndDelta(pid: String, delta: Int, now: Long) {
        val row = byId(pid)
        if (row == null) {
            upsert(StockState(productId = pid, serverQty = 0, localDelta = delta, updatedAt = now))
        } else {
            applyDelta(pid, delta, now)
        }
    }

    @Transaction
    fun ensureAndSetServer(pid: String, serverQty: Int, now: Long) {
        val row = byId(pid)
        if (row == null) {
            upsert(StockState(productId = pid, serverQty = serverQty, localDelta = 0, updatedAt = now))
        } else {
            updateServerQty(pid, serverQty, now)
        }
    }
}
