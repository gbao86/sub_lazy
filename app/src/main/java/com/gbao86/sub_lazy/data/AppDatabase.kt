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

@Database(entities = [Subscription::class, PaymentHistory::class], version = 7, exportSchema = true)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "subscription_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
