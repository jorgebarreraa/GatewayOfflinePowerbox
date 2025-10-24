package cl.powerbox.gateway.data.dao

import androidx.room.*
import cl.powerbox.gateway.data.entity.PendingRequest

@Dao
interface PendingRequestDao {
    @Insert
    suspend fun insert(p: PendingRequest)

    @Query("SELECT * FROM pending_request WHERE done = 0 ORDER BY createdAt ASC LIMIT :limit")
    suspend fun nextBatch(limit: Int): List<PendingRequest>

    @Query("UPDATE pending_request SET done = 1 WHERE id = :id")
    suspend fun markDone(id: String)

    @Query("UPDATE pending_request SET attempts = attempts + 1, lastError = :err WHERE id = :id")
    suspend fun markAttempt(id: String, err: String?)
}
