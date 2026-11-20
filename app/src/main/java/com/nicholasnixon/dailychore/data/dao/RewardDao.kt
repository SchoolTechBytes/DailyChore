package com.nicholasnixon.dailychore.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nicholasnixon.dailychore.data.entities.Reward
import kotlinx.coroutines.flow.Flow

/** Database operations for rewards. Same pattern as ChoreDao. */
@Dao
interface RewardDao {

    // Cheapest rewards first, so the kid sees what they can afford soonest.
    @Query("SELECT * FROM rewards ORDER BY cost, title COLLATE NOCASE")
    fun getAll(): Flow<List<Reward>>

    @Insert
    suspend fun insert(reward: Reward): Long

    @Update
    suspend fun update(reward: Reward)

    @Delete
    suspend fun delete(reward: Reward)
}
