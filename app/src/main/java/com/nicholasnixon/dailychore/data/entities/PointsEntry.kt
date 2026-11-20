package com.nicholasnixon.dailychore.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One line in the points history (like a bank statement).
 *
 * - Finishing a chore adds an entry with a positive amount and that chore's id.
 * - Redeeming a reward adds an entry with a negative amount and choreId = null.
 *
 * The balance is the sum of all amounts. A chore is "done today" when it has an
 * entry with today's date, so a new day automatically starts with nothing ticked.
 *
 * The foreign key links choreId to the chores table. SET_NULL means deleting a
 * chore keeps its old entries (and the points) but clears their choreId.
 * The index makes "find entries for this chore" lookups fast.
 */
@Entity(
    tableName = "points_entries",
    foreignKeys = [
        ForeignKey(
            entity = Chore::class,
            parentColumns = ["id"],
            childColumns = ["choreId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("choreId")]
)
data class PointsEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val choreId: Long?,
    val description: String,
    val amount: Int,
    // Stored as "yyyy-MM-dd" text, e.g. "2026-11-20".
    val date: String
)
