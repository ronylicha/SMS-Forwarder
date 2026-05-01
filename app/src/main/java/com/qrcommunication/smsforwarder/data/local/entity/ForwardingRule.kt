package com.qrcommunication.smsforwarder.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DestinationType {
    SMS,
    WEBHOOK;

    companion object {
        fun fromValue(value: String): DestinationType =
            entries.firstOrNull { it.name == value } ?: SMS
    }
}

@Entity(tableName = "forwarding_rules")
data class ForwardingRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "priority")
    val priority: Int = 0,

    /** Regex pour matcher l'expediteur. Null = match tous. */
    @ColumnInfo(name = "sender_pattern")
    val senderPattern: String? = null,

    /** Mot-cle ou regex a chercher dans le contenu. Null = pas de filtre contenu. */
    @ColumnInfo(name = "keyword_pattern")
    val keywordPattern: String? = null,

    @ColumnInfo(name = "destination_type")
    val destinationType: String = DestinationType.SMS.name,

    /** Telephone ou URL webhook selon destinationType. */
    @ColumnInfo(name = "destination")
    val destination: String,

    @ColumnInfo(name = "is_enabled")
    val isEnabled: Boolean = true,

    @ColumnInfo(name = "success_count")
    val successCount: Int = 0,

    @ColumnInfo(name = "failure_count")
    val failureCount: Int = 0,

    @ColumnInfo(name = "last_success_at")
    val lastSuccessAt: Long? = null,

    @ColumnInfo(name = "last_error_at")
    val lastErrorAt: Long? = null,

    @ColumnInfo(name = "last_error_message")
    val lastErrorMessage: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
