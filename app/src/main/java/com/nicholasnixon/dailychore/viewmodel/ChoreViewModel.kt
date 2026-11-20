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
import com.nicholasnixon.dailychore.data.repository.PinRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Sits between the screens and the repositories.
 * - It survives screen rotation, so data isn't reloaded every time the phone turns.
 * - Screens read [uiState] and call the action functions below; they never touch storage.
 */
@OptIn(ExperimentalCoroutinesApi::class) // for flatMapLatest
class ChoreViewModel(
    private val repository: ChoreRepository,
    private val pinRepository: PinRepository
) : ViewModel() {

    /** Today's date. When it changes (midnight), the "done today" list switches to the new day. */
    private val today = MutableStateFlow(repository.today())

    /** Parent lock. Starts locked every time the app starts. */
    private data class LockState(val hasPin: Boolean, val isUnlocked: Boolean)
    private val lock = MutableStateFlow(LockState(hasPin = pinRepository.hasPin(), isUnlocked = false))

    /**
     * combine: whenever any of the 5 streams changes, build a new ChoreUiState.
     * flatMapLatest: whenever `today` changes, stop watching the old day's ticks and
     * start watching the new day's (that's what makes ticks reset at midnight).
     * stateIn: turns it into a StateFlow (always has a current value) that Compose can watch.
     * WhileSubscribed(5_000): stop the database queries 5 seconds after no screen is watching.
     */
    val uiState: StateFlow<ChoreUiState> = combine(
        repository.chores,
        repository.rewards,
        repository.balance,
        today.flatMapLatest { date -> repository.completedIdsOn(date) },
        lock
    ) { chores, rewards, balance, doneIds, lock ->
        ChoreUiState(
            chores = chores,
            rewards = rewards,
            balance = balance,
            completedTodayIds = doneIds.toSet(),
            hasPin = lock.hasPin,
            isParentUnlocked = lock.isUnlocked
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChoreUiState(hasPin = pinRepository.hasPin())
    )

    // A short message for the user (e.g. "Not enough points"). null = nothing to show.
    // The private Mutable version can be changed here; screens only get the read-only one.
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        // Check the date once a minute while the app is open, so ticks reset just after midnight.
        // (MutableStateFlow ignores setting the same value, so nothing reloads until the day changes.)
        viewModelScope.launch {
            while (isActive) {
                delay(60_000)
                refreshDate()
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    /** Re-reads today's date (also called when the app comes back to the foreground). */
    fun refreshDate() {
        today.value = repository.today()
    }

    // --- Chore & reward actions ---
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

    // --- Parent PIN ---

    /** Saves a new PIN (first-time setup) and opens the Parent screen. */
    fun createPin(pin: String) {
        if (!isValidPin(pin)) return
        pinRepository.setPin(pin)
        lock.value = LockState(hasPin = true, isUnlocked = true)
    }

    /** @return true and unlocks the Parent screen if [pin] is right; false if it's wrong. */
    fun unlockParent(pin: String): Boolean {
        val correct = pinRepository.checkPin(pin)
        if (correct) lock.update { it.copy(isUnlocked = true) }
        return correct
    }

    /** @return true if the PIN was changed; false if [currentPin] was wrong or [newPin] invalid. */
    fun changePin(currentPin: String, newPin: String): Boolean {
        if (!isValidPin(newPin) || !pinRepository.checkPin(currentPin)) return false
        pinRepository.setPin(newPin)
        _message.value = "PIN changed"
        return true
    }

    fun lockParent() {
        lock.update { it.copy(isUnlocked = false) }
    }

    /** "Forgot PIN?": deletes ALL chores, rewards, points AND the PIN. */
    fun resetEverything() {
        viewModelScope.launch {
            repository.eraseAll()
            pinRepository.clearPin()
            lock.value = LockState(hasPin = false, isUnlocked = false)
            _message.value = "Everything was erased. Create a new PIN."
        }
    }

    private fun isValidPin(pin: String) = pin.length == PIN_LENGTH && pin.all { it.isDigit() }

    /**
     * Our ViewModel needs repositories passed in, which Android can't do by itself.
     * This factory tells it how: get our Application object and use its repositories.
     * Pattern from developer.android.com: "Create ViewModels with dependencies".
     */
    companion object {
        const val PIN_LENGTH = 4

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as DailyChoreApplication
                ChoreViewModel(app.repository, app.pinRepository)
            }
        }
    }
}
