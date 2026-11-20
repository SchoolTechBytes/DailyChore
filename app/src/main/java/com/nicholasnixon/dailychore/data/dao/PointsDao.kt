package com.nicholasnixon.dailychore.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nicholasnixon.dailychore.data.entities.PointsEntry
import kotlinx.coroutines.flow.Flow

/** Database operations for the points history. */
@Dao
interface PointsDao {

    @Insert
    suspend fun insert(entry: PointsEntry): Long

    // COALESCE turns "no rows" (SUM = NULL) into 0.
    @Query("SELECT COALESCE(SUM(amount), 0) FROM points_entries")
    fun getBalance(): Flow<Int>

    // Same query, read once instead of watched. Used to check "can the kid afford this?"
    @Query("SELECT COALESCE(SUM(amount), 0) FROM points_entries")
    suspend fun getBalanceNow(): Int

    // :date is replaced by the function parameter with the same name.
    @Query("SELECT choreId FROM points_entries WHERE date = :date AND choreId IS NOT NULL")
    fun getCompletedChoreIds(date: String): Flow<List<Long>>

    @Query("SELECT COUNT(*) FROM points_entries WHERE choreId = :choreId AND date = :date")
    suspend fun countCompletions(choreId: Long, date: String): Int

    // Un-ticks a chore that was ticked by mistake today (removes its points too).
    @Query("DELETE FROM points_entries WHERE choreId = :choreId AND date = :date")
    suspend fun deleteCompletion(choreId: Long, date: String)
}
