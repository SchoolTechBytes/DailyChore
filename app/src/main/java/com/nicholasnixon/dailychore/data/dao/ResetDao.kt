package com.nicholasnixon.dailychore.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

/**
 * Wipes all app data (used by "Forgot PIN?" → Erase & reset).
 *
 * This DAO is an abstract class instead of an interface so it can have a real function
 * ([eraseAll]) that calls the three delete queries. @Transaction makes those three run as one
 * step: either everything is deleted, or (if something goes wrong) nothing is.
 */
@Dao
abstract class ResetDao {

    @Query("DELETE FROM points_entries")
    abstract suspend fun deleteAllPoints()

    @Query("DELETE FROM chores")
    abstract suspend fun deleteAllChores()

    @Query("DELETE FROM rewards")
    abstract suspend fun deleteAllRewards()

    @Transaction
    open suspend fun eraseAll() {
        // Points first: they point at chores (foreign key).
        deleteAllPoints()
        deleteAllChores()
        deleteAllRewards()
    }
}
