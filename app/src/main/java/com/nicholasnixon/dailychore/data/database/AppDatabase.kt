package com.nicholasnixon.dailychore.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nicholasnixon.dailychore.data.dao.ChoreDao
import com.nicholasnixon.dailychore.data.dao.PointsDao
import com.nicholasnixon.dailychore.data.dao.ResetDao
import com.nicholasnixon.dailychore.data.dao.RewardDao
import com.nicholasnixon.dailychore.data.entities.Chore
import com.nicholasnixon.dailychore.data.entities.PointsEntry
import com.nicholasnixon.dailychore.data.entities.Reward

/**
 * The Room database: one file on the phone ("daily_chore.db") holding all three tables.
 * Data saved here survives closing the app and restarting the phone.
 *
 * - entities: the tables in this database.
 * - version: bump this when the tables change (and add a migration).
 * - exportSchema = false: we don't keep schema history files, which is fine for version 1.
 */
@Database(
    entities = [Chore::class, Reward::class, PointsEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun choreDao(): ChoreDao
    abstract fun rewardDao(): RewardDao
    abstract fun pointsDao(): PointsDao
    abstract fun resetDao(): ResetDao

    // companion object = things that belong to the class itself, like Java "static".
    companion object {
        // @Volatile: every thread sees the latest value straight away.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the one shared database (a "singleton"). Opening a database is slow,
         * so the app should only ever open it once.
         */
        fun getDatabase(context: Context): AppDatabase {
            // If it already exists, return it; otherwise build it inside synchronized,
            // so two threads can't both create one at the same time.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daily_chore.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
