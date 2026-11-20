package com.nicholasnixon.dailychore.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A reward the kid can buy with points, e.g. "30 min screen time" costing 20 points.
 * Stored in the "rewards" table.
 */
@Entity(tableName = "rewards")
data class Reward(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val cost: Int
)
