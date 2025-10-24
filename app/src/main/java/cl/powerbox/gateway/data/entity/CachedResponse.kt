package cl.powerbox.gateway.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "cached_response")
data class CachedResponse(
    @PrimaryKey val key: String,
    val path: String,
    val method: String,
    val bodyHash: String,
    val contentType: String,

    // Guardamos el cuerpo como BLOB en Room
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val bytes: ByteArray,

    // ⬇️ NUEVOS CAMPOS con valores por defecto para evitar errores de compilación
    val cachedAt: Long = System.currentTimeMillis(),
    val lastHitAt: Long = cachedAt
)