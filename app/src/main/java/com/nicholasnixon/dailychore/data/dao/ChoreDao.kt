package com.nicholasnixon.dailychore.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.nicholasnixon.dailychore.data.entities.Chore
import kotlinx.coroutines.flow.Flow

/**
 * DAO = Data Access Object: the list of database operations for chores.
 * We only write the interface; Room generates the real code at build time (via KSP).
 *
 * - Functions returning Flow are "live": they emit a new list every time the table changes,
 *   so the screen updates by itself.
 * - suspend functions run in a coroutine, so the database work never freezes the UI.
 */
@Dao
interface ChoreDao {

    @Query("SELECT * FROM chores ORDER BY title COLLATE NOCASE")
    fun getAll(): Flow<List<Chore>>

    @Insert
    suspend fun insert(chore: Chore): Long

    @Update
    suspend fun update(chore: Chore)

    @Delete
    suspend fun delete(chore: Chore)
}
