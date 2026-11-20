package com.nicholasnixon.dailychore.viewmodel

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nicholasnixon.dailychore.data.database.AppDatabase
import com.nicholasnixon.dailychore.data.entities.Reward
import com.nicholasnixon.dailychore.data.repository.ChoreRepository
import com.nicholasnixon.dailychore.data.repository.PinRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

/**
 * Checks the ViewModel against a real (in-memory) database and a test-only PIN file.
 * Runs on an emulator/device.
 */
@RunWith(AndroidJUnit4::class)
class ChoreViewModelTest {

    private lateinit var db: AppDatabase
    private lateinit var pins: PinRepository
    private lateinit var vm: ChoreViewModel
    private var now = Date() // fake clock

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val repository = ChoreRepository(
            db.choreDao(), db.rewardDao(), db.pointsDao(), db.resetDao(),
            clock = { now }
        )
        pins = PinRepository(context, prefsName = "pin_vm_test").also { it.clearPin() }
        // viewModelScope uses the main thread, so create the ViewModel there.
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            vm = ChoreViewModel(repository, pins)
        }
    }

    @After
    fun tearDown() {
        pins.clearPin()
        db.close()
    }

    // Waits (max 5s) until uiState matches the condition.
    private suspend fun awaitState(check: (ChoreUiState) -> Boolean) =
        withTimeout(5_000) { vm.uiState.first(check) }

    @Test
    fun tickingChoreUpdatesState() = runBlocking<Unit> {
        vm.addChore("  Brush teeth  ", 3)
        vm.addChore("", 5)          // ignored: blank title
        vm.addChore("Nothing", 0)   // ignored: zero points

        val chore = awaitState { it.chores.isNotEmpty() }.chores.single()
        assertEquals("Brush teeth", chore.title)

        vm.toggleChore(chore)
        // Balance and "done today" come from two separate queries that can update a few ms apart,
        // so wait until BOTH have caught up.
        val done = awaitState { it.balance == 3 && chore.id in it.completedTodayIds }
        assertEquals(setOf(chore.id), done.completedTodayIds)
    }

    @Test
    fun redeemWithoutPointsShowsMessage() = runBlocking<Unit> {
        vm.redeemReward(Reward(title = "Ice cream", cost = 10))
        val msg = withTimeout(5_000) { vm.message.first { it != null } }
        assertEquals("Not enough points for Ice cream", msg)
    }

    @Test
    fun ticksResetAfterMidnight() = runBlocking<Unit> {
        vm.addChore("Make bed", 5)
        val chore = awaitState { it.chores.isNotEmpty() }.chores.single()
        vm.toggleChore(chore)
        awaitState { chore.id in it.completedTodayIds }

        now = Date(now.time + 24L * 60 * 60 * 1000) // tomorrow
        vm.refreshDate()

        val next = awaitState { it.completedTodayIds.isEmpty() }
        assertEquals(5, next.balance) // points earned yesterday are kept
    }

    @Test
    fun parentStartsLockedAndPinUnlocksIt() = runBlocking<Unit> {
        val start = awaitState { true }
        assertFalse(start.hasPin)
        assertFalse(start.isParentUnlocked)

        vm.createPin("1234")
        awaitState { it.hasPin && it.isParentUnlocked }

        vm.lockParent()
        awaitState { !it.isParentUnlocked }

        assertFalse(vm.unlockParent("0000"))
        assertTrue(vm.unlockParent("1234"))
        awaitState { it.isParentUnlocked }
    }

    @Test
    fun changePinNeedsTheCurrentPin() {
        vm.createPin("1234")
        assertFalse(vm.changePin("9999", "5678")) // wrong current PIN
        assertTrue(vm.changePin("1234", "5678"))
        assertFalse(vm.unlockParent("1234"))
        assertTrue(vm.unlockParent("5678"))
    }

    @Test
    fun resetEverythingErasesDataAndPin() = runBlocking<Unit> {
        vm.createPin("1234")
        vm.addChore("Make bed", 5)
        vm.addReward("Sticker", 5)
        awaitState { it.chores.isNotEmpty() && it.rewards.isNotEmpty() }

        vm.resetEverything()

        val after = awaitState { !it.hasPin && it.chores.isEmpty() && it.rewards.isEmpty() }
        assertFalse(after.isParentUnlocked)
        assertEquals(0, after.balance)
        assertFalse(pins.hasPin())
    }
}
