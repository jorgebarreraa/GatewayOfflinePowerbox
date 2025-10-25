package cl.powerbox.gateway.data.dao

import androidx.room.*
import cl.powerbox.gateway.data.entity.ReplenishmentEvent

@Dao
interface ReplenishmentEventDao {

    @Query("SELECT * FROM replenishment_events ORDER BY createdAt DESC")
    fun all(): List<ReplenishmentEvent>

    @Query("SELECT * FROM replenishment_events WHERE sent = 0 ORDER BY createdAt ASC")
    fun allUnsent(): List<ReplenishmentEvent>

    @Query("SELECT * FROM replenishment_events WHERE sent = 1 ORDER BY createdAt DESC")
    fun allSent(): List<ReplenishmentEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(event: ReplenishmentEvent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertAll(events: List<ReplenishmentEvent>)

    @Query("UPDATE replenishment_events SET sent = 1 WHERE id = :eventId")
    fun markAsSent(eventId: String)

    @Query("DELETE FROM replenishment_events WHERE sent = 1 AND createdAt < :timestamp")
    fun deleteOldSent(timestamp: Long)

    @Query("DELETE FROM replenishment_events WHERE id = :id")
    fun deleteById(id: String)
}