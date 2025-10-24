package cl.powerbox.gateway.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_request")
data class PendingRequest(
    @PrimaryKey val id: String,
    val path: String,
    val method: String,
    val headersJson: String,
    val body: ByteArray,
    val clientOrderId: String?,
    val createdAt: Long,
    val done: Boolean = false,
    val attempts: Int = 0,
    val lastError: String? = null
)
