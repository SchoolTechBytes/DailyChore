package com.nicholasnixon.dailychore.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nicholasnixon.dailychore.DailyChoreApplication
import com.nicholasnixon.dailychore.data.entities.Chore
import com.nicholasnixon.dailychore.data.entities.Reward
import com.nicholasnixon.dailychore.data.repository.ChoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Sits between the screens and the repository.
 * - It survives screen rotation, so data isn't reloaded every time the phone turns.
 * - Screens read [uiState] and call the action functions below; they never touch the database.
 */
class ChoreViewModel(private val repository: ChoreRepository) : ViewModel() {

    /**
     * combine: whenever any of the 4 database streams changes, build a new ChoreUiState.
     * stateIn: turns it into a StateFlow (always has a current value) that Compose can watch.
     * WhileSubscribed(5_000): stop the database queries 5 seconds after no screen is watching.
     * The 5 seconds means a rotation (screen briefly gone) doesn't restart them.
     */
    val uiState: StateFlow<ChoreUiState> = combine(
        repository.chores,
        repository.rewards,
        repository.balance,
        repository.completedTodayIds()
    ) { chores, rewards, balance, doneIds ->
        ChoreUiState(
            chores = chores,
            rewards = rewards,
            balance = balance,
            completedTodayIds = doneIds.toSet()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChoreUiState()
    )

    // A short message for the user (e.g. "Not enough points"). null = nothing to show.
    // The private Mutable version can be changed here; screens only get the read-only one.
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearMessage() {
        _message.value = null
    }

    // --- Actions ---
    // viewModelScope.launch starts a coroutine: the database work runs without freezing
    // the screen, and is cancelled automatically if this ViewModel is destroyed.

    /** Ticks a chore if it isn't done today, un-ticks it if it is. */
    fun toggleChore(chore: Chore) {
        val isDone = chore.id in uiState.value.completedTodayIds
        viewModelScope.launch {
            if (isDone) repository.undoChore(chore) else repository.completeChore(chore)
        }
    }

    fun addChore(title: String, points: Int) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty() || points <= 0) return
        viewModelScope.launch { repository.addChore(cleanTitle, points) }
    }

    fun deleteChore(chore: Chore) {
        viewModelScope.launch { repository.deleteChore(chore) }
    }

    fun addReward(title: String, cost: Int) {
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty() || cost <= 0) return
        viewModelScope.launch { repository.addReward(cleanTitle, cost) }
    }

    fun deleteReward(reward: Reward) {
        viewModelScope.launch { repository.deleteReward(reward) }
    }

    fun redeemReward(reward: Reward) {
        viewModelScope.launch {
            val worked = repository.redeemReward(reward)
            _message.value =
                if (worked) "Enjoy: ${reward.title}!" else "Not enough points for ${reward.title}"
        }
    }

    /**
     * Our ViewModel needs a repository passed in, which Android can't do by itself.
     * This factory tells it how: get our Application object and use its repository.
     * Pattern from developer.android.com: "Create ViewModels with dependencies".
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as DailyChoreApplication
                ChoreViewModel(app.repository)
            }
        }
    }
}
