/*
Copyright (C) 2026 Trịnh Gia Bảo (gbao86) <tiktokthu10@gmail.com>. All Rights Reserved.

This file is part of Sub Lazy - A premium, modern subscription tracker and manager for Android.

This source code is licensed under the Non-Commercial License terms.
You are permitted to use, copy, and modify this software for personal, educational, 
and non-commercial purposes. 
Commercial exploitation, sale, or distribution of this software or any derivative works 
is strictly prohibited without the express written permission of the author.
*/

package com.gbao86.sub_lazy.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Subscription::class, PaymentHistory::class, SharedMember::class], version = 9, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN currency TEXT NOT NULL DEFAULT 'VND'")
                db.execSQL("UPDATE subscriptions SET currency = 'USD' WHERE amount <= 1000")
            }
        }

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN remainingTimes INTEGER DEFAULT NULL")
            }
        }

        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `payment_history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `subscriptionId` INTEGER NOT NULL, 
                        `subscriptionName` TEXT NOT NULL, 
                        `amount` REAL NOT NULL, 
                        `currency` TEXT NOT NULL, 
                        `paymentDate` INTEGER NOT NULL, 
                        `cycle` TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN isKmBased INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN lastOdometer REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN targetIntervalKm REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN dailyAverageKm REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN lastOdometerUpdateDate INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN bankAccount TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN bankName TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN bankAccountHolder TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN isSessionBased INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN totalSessions INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN remainingSessions INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN isInstallment INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN totalInstallmentPeriods INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN isShared INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE subscriptions ADD COLUMN sharedMembersJson TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Recreate payment_history to add foreign key constraint and index
                db.execSQL("ALTER TABLE payment_history RENAME TO temp_payment_history")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `payment_history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `subscriptionId` INTEGER NOT NULL, 
                        `subscriptionName` TEXT NOT NULL, 
                        `amount` REAL NOT NULL, 
                        `currency` TEXT NOT NULL, 
                        `paymentDate` INTEGER NOT NULL, 
                        `cycle` TEXT NOT NULL,
                        FOREIGN KEY(`subscriptionId`) REFERENCES `subscriptions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO payment_history (`id`, `subscriptionId`, `subscriptionName`, `amount`, `currency`, `paymentDate`, `cycle`)
                    SELECT `id`, `subscriptionId`, `subscriptionName`, `amount`, `currency`, `paymentDate`, `cycle` FROM temp_payment_history
                """.trimIndent())
                db.execSQL("DROP TABLE temp_payment_history")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_history_subscriptionId` ON `payment_history` (`subscriptionId`)")
            }
        }

        /**
         * MIGRATION_7_8: Recreate subscriptions table to fix schema mismatches.
         *
         * Previous migrations used ALTER TABLE which leaves behind DEFAULT values
         * in SQLite metadata (e.g. DEFAULT 0, DEFAULT NULL) that don't match what
         * Room expects ('undefined'). Also adds missing indices on category and cycle.
         *
         * Fix: recreate the table from scratch without column-level defaults
         * (except for columns with @ColumnInfo(defaultValue)) and add indices.
         */
        val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // 1. Rename old table
                db.execSQL("ALTER TABLE subscriptions RENAME TO temp_subscriptions")

                // 2. Create new table matching the entity definition exactly
                //    - Columns with @ColumnInfo(defaultValue = "0"): isKmBased, isSessionBased, isInstallment, isShared
                //    - Column with @ColumnInfo(defaultValue = "VND"): currency
                //    - All other columns: no DEFAULT (Room expects 'undefined')
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `subscriptions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `nextBillingDate` INTEGER NOT NULL,
                        `cycle` TEXT NOT NULL,
                        `category` TEXT NOT NULL,
                        `colorHex` TEXT NOT NULL,
                        `iconName` TEXT,
                        `currency` TEXT NOT NULL DEFAULT 'VND',
                        `remainingTimes` INTEGER,
                        `isKmBased` INTEGER NOT NULL DEFAULT 0,
                        `lastOdometer` REAL,
                        `targetIntervalKm` REAL,
                        `dailyAverageKm` REAL,
                        `lastOdometerUpdateDate` INTEGER,
                        `bankAccount` TEXT,
                        `bankName` TEXT,
                        `bankAccountHolder` TEXT,
                        `isSessionBased` INTEGER NOT NULL DEFAULT 0,
                        `totalSessions` INTEGER,
                        `remainingSessions` INTEGER,
                        `isInstallment` INTEGER NOT NULL DEFAULT 0,
                        `totalInstallmentPeriods` INTEGER,
                        `isShared` INTEGER NOT NULL DEFAULT 0,
                        `sharedMembersJson` TEXT
                    )
                """.trimIndent())

                // 3. Copy data from old table
                db.execSQL("""
                    INSERT INTO `subscriptions` (
                        `id`, `name`, `amount`, `nextBillingDate`, `cycle`, `category`,
                        `colorHex`, `iconName`, `currency`, `remainingTimes`,
                        `isKmBased`, `lastOdometer`, `targetIntervalKm`, `dailyAverageKm`,
                        `lastOdometerUpdateDate`, `bankAccount`, `bankName`, `bankAccountHolder`,
                        `isSessionBased`, `totalSessions`, `remainingSessions`,
                        `isInstallment`, `totalInstallmentPeriods`,
                        `isShared`, `sharedMembersJson`
                    )
                    SELECT
                        `id`, `name`, `amount`, `nextBillingDate`, `cycle`, `category`,
                        `colorHex`, `iconName`, `currency`, `remainingTimes`,
                        `isKmBased`, `lastOdometer`, `targetIntervalKm`, `dailyAverageKm`,
                        `lastOdometerUpdateDate`, `bankAccount`, `bankName`, `bankAccountHolder`,
                        `isSessionBased`, `totalSessions`, `remainingSessions`,
                        `isInstallment`, `totalInstallmentPeriods`,
                        `isShared`, `sharedMembersJson`
                    FROM `temp_subscriptions`
                """.trimIndent())

                // 4. Drop old table
                db.execSQL("DROP TABLE `temp_subscriptions`")

                // 5. Create indices matching entity definition
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_subscriptions_category` ON `subscriptions` (`category`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_subscriptions_cycle` ON `subscriptions` (`cycle`)")
            }
        }

        /**
         * MIGRATION_8_9: Extract shared_members from JSON string into a proper Room Entity.
         *
         * Creates the shared_members table with ForeignKey CASCADE to subscriptions.
         * Data migration is handled at app startup via MigrateSharedMembersUseCase
         * (reads sharedMembersJson, inserts rows, clears the JSON column).
         * The sharedMembersJson column is kept for now and nulled out after migration.
         */
        val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `shared_members` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `subscriptionId` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `hasPaid` INTEGER NOT NULL DEFAULT 0,
                        `phone` TEXT,
                        FOREIGN KEY(`subscriptionId`) REFERENCES `subscriptions`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_shared_members_subscriptionId` ON `shared_members` (`subscriptionId`)")

                // Migrate existing JSON data into the new table
                val cursor = db.query("SELECT id, sharedMembersJson FROM subscriptions WHERE isShared = 1 AND sharedMembersJson IS NOT NULL AND sharedMembersJson != ''")
                try {
                    while (cursor.moveToNext()) {
                        val subId = cursor.getLong(0)
                        val json = cursor.getString(1) ?: continue
                        // Parse semicolon-delimited format: "EncodedName:amount:hasPaid:EncodedPhone;..."
                        val entries = json.split(";").filter { it.isNotBlank() }
                        for (entry in entries) {
                            val parts = entry.split(":")
                            if (parts.isEmpty()) continue
                            val name = android.net.Uri.decode(parts[0])
                            val amount = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0
                            val hasPaid = if (parts.getOrNull(2)?.toBoolean() == true) 1 else 0
                            val phone = parts.getOrNull(3)?.let { android.net.Uri.decode(it) }?.takeIf { it.isNotBlank() }
                            db.execSQL(
                                "INSERT INTO shared_members (subscriptionId, name, amount, hasPaid, phone) VALUES (?, ?, ?, ?, ?)",
                                arrayOf<Any?>(subId, name, amount, hasPaid, phone)
                            )
                        }
                        // Clear the JSON column after migration
                        db.execSQL("UPDATE subscriptions SET sharedMembersJson = NULL WHERE id = ?", arrayOf(subId))
                    }
                } finally {
                    cursor.close()
                }
            }
        }

    }
}
