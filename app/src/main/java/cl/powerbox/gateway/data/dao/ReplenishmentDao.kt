package cl.powerbox.gateway.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.powerbox.gateway.data.entity.ReplenishmentEvent

@Dao
interface ReplenishmentDao {

    // Inserción (o reemplazo) por lote — usamos REPLACE para idempotencia local.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(events: List<ReplenishmentEvent>)

    // Inserta uno (por si lo necesitas en otras partes)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(e: ReplenishmentEvent)

    // Pendientes de enviar al panel (ordenados por fecha)
    @Query("""
        SELECT *
        FROM replenishment_event
        WHERE sent = 0
        ORDER BY createdAt ASC
    """)
    suspend fun pending(): List<ReplenishmentEvent>

    // Marca como enviados
    @Query("UPDATE replenishment_event SET sent = 1 WHERE id IN (:ids)")
    suspend fun markSent(ids: List<String>)
}