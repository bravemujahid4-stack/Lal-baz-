package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CustomerEntity::class,
        MarketRatesEntity::class,
        RateHistoryEntity::class,
        NotificationEntity::class,
        AdminEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun marketRatesDao(): MarketRatesDao
    abstract fun rateHistoryDao(): RateHistoryDao
    abstract fun notificationDao(): NotificationDao
    abstract fun adminDao(): AdminDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mujahid_accounts_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
