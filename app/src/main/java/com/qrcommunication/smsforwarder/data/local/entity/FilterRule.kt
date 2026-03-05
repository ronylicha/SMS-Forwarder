package com.qrcommunication.smsforwarder.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class FilterType(val value: String) {
    WHITELIST("WHITELIST"),
    BLACKLIST("BLACKLIST");

    companion object {
        fun fromValue(value: String): FilterType {
            return entries.firstOrNull { it.value == value }
                ?: throw IllegalArgumentException("Unknown FilterType: $value")
        }
    }
}

@Entity(tableName = "filter_rules")
data class FilterRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "pattern")
    val pattern: String,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
)
