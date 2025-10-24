package cl.powerbox.gateway.data.dao

import androidx.room.*
import cl.powerbox.gateway.data.entity.CachedResponse

@Dao
interface CachedResponseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(e: CachedResponse)

    @Query("SELECT * FROM cached_response WHERE key = :key LIMIT 1")
    suspend fun byKey(key: String): CachedResponse?

    @Query("UPDATE cached_response SET lastHitAt = :ts WHERE key = :key")
    suspend fun touch(key: String, ts: Long)

    // TTL: borra por antigüedad
    @Query("DELETE FROM cached_response WHERE cachedAt < :olderThanMs")
    suspend fun deleteOlderThan(olderThanMs: Long): Int

    // Orden por menos usados (LRU)
    @Query("SELECT key FROM cached_response ORDER BY lastHitAt ASC LIMIT :limit")
    suspend fun lruKeys(limit: Int): List<String>

    @Query("DELETE FROM cached_response WHERE key IN (:keys)")
    suspend fun deleteByKeys(keys: List<String>): Int

    // Tamaño total (usamos LENGTH() para contar bytes del BLOB)
    @Query("SELECT IFNULL(SUM(LENGTH(bytes)), 0) FROM cached_response")
    suspend fun totalBytes(): Long
}