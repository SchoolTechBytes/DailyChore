package com.nicholasnixon.dailychore.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A chore the parent sets up, e.g. "Make bed" worth 5 points.
 *
 * @Entity tells Room to make a database table from this class.
 * Each property becomes a column in the "chores" table.
 */
@Entity(tableName = "chores")
data class Chore(
    // autoGenerate = true: Room picks the next id when we insert with id = 0.
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val points: Int
)
