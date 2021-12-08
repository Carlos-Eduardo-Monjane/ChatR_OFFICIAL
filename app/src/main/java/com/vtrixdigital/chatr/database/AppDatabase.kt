package com.vtrixdigital.chatr.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(entities = [Rules::class, SentMessages::class, BulkImportList::class], version = 3)
abstract class AppDatabase : RoomDatabase() {

    abstract fun rulesDao(): RulesDao
    abstract fun sentMessageDao(): SentMessagesDAO
    abstract fun bulkImportDao(): BulkImportDAO

    companion object {
        private var instance: AppDatabase? = null
        private const val database: String = "whatsappAutoReply.db"
        private val migration1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE bulkImportList (id INTEGER, countryCode TEXT NOT NULL, receiver TEXT NOT NULL,message TEXT NOT NULL,isSent INTEGER NOT NULL,appName TEXT NOT NULL,campaignName TEXT NOT NULL,source TEXT NOT NULL,sentTime TEXT DEFAULT NULL,createdAt TEXT DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY(id))");            }
        }

        private val migration2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE bulkImportList ADD COLUMN messageType TEXT DEFAULT NULL")
                database.execSQL("ALTER TABLE bulkImportList ADD COLUMN fileUri TEXT DEFAULT NULL")
            }
        }


        fun getInstance(context: Context) : AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    database
                ).addMigrations(migration1_2)
                 .addMigrations(migration2_3)
                .build()
            }
            return instance!!
        }
    }
}