package cl.powerbox.gateway.data.dao

import androidx.room.*
import cl.powerbox.gateway.data.entity.StockState

@Dao
interface StockStateDao {

    @Query("SELECT * FROM stock_state")
    fun all(): List<StockState>

    @Query("SELECT * FROM stock_state WHERE productId = :id LIMIT 1")
    fun byId(id: String): StockState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(state: StockState)

    @Query("UPDATE stock_state SET serverQty = :qty WHERE productId = :id")
    fun updateServerQty(id: String, qty: Int)

    @Query("UPDATE stock_state SET localDelta = 0 WHERE productId = :id")
    fun resetLocalDelta(id: String)

    @Query("DELETE FROM stock_state WHERE productId = :id")
    fun deleteById(id: String)

    @Transaction
    fun ensureAndDelta(productId: String, delta: Int, timestamp: Long) {
        val existing = byId(productId)
        if (existing == null) {
            upsert(
                StockState(
                    productId = productId,
                    serverQty = 0,
                    localDelta = delta,
                    lastSync = timestamp
                )
            )
        } else {
            val newDelta = existing.localDelta + delta
            upsert(existing.copy(localDelta = newDelta, lastSync = timestamp))
        }
    }
}