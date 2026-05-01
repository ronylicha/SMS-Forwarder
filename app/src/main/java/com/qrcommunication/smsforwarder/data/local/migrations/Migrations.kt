package com.qrcommunication.smsforwarder.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 -> v2 :
 *  - Ajout colonne `rule_id` a sms_records (lie un transfert a la regle qui l'a declenche).
 *  - Creation table forwarding_rules (regles de routage avec destination par type).
 *  - Creation table app_notifications (centre de notifications in-app).
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE sms_records ADD COLUMN rule_id INTEGER")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS forwarding_rules (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                priority INTEGER NOT NULL DEFAULT 0,
                sender_pattern TEXT,
                keyword_pattern TEXT,
                destination_type TEXT NOT NULL DEFAULT 'SMS',
                destination TEXT NOT NULL,
                is_enabled INTEGER NOT NULL DEFAULT 1,
                success_count INTEGER NOT NULL DEFAULT 0,
                failure_count INTEGER NOT NULL DEFAULT 0,
                last_success_at INTEGER,
                last_error_at INTEGER,
                last_error_message TEXT,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS app_notifications (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                type TEXT NOT NULL,
                severity TEXT NOT NULL,
                title TEXT NOT NULL,
                message TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                is_read INTEGER NOT NULL DEFAULT 0,
                action_route TEXT,
                rule_id INTEGER,
                record_id INTEGER
            )
            """.trimIndent()
        )

        db.execSQL(
            "CREATE INDEX IF NOT EXISTS index_app_notifications_is_read_timestamp " +
                "ON app_notifications(is_read, timestamp)"
        )
    }
}
