package com.nicholasnixon.dailychore.viewmodel

import com.nicholasnixon.dailychore.data.entities.Chore
import com.nicholasnixon.dailychore.data.entities.Reward

/**
 * One snapshot of everything the screens need to draw.
 * It's immutable (all `val`): when data changes, the ViewModel makes a new copy
 * and Compose redraws whatever changed.
 */
data class ChoreUiState(
    val chores: List<Chore> = emptyList(),
    val rewards: List<Reward> = emptyList(),
    val balance: Int = 0,
    // A Set makes "is this chore done today?" a quick check: chore.id in completedTodayIds
    val completedTodayIds: Set<Long> = emptySet()
)
