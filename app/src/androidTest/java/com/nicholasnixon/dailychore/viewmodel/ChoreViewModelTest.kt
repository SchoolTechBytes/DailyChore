package com.nicholasnixon.dailychore.viewmodel

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nicholasnixon.dailychore.data.database.AppDatabase
import com.nicholasnixon.dailychore.data.entities.Reward
import com.nicholasnixon.dailychore.data.repository.ChoreRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Checks the ViewModel against a real (in-memory) database. Runs on an emulator/device. */
@RunWith(AndroidJUnit4::class)
class ChoreViewModelTest {

    private lateinit var db: AppDatabase
    private lateinit var vm: ChoreViewModel

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val repository = ChoreRepository(db.choreDao(), db.rewardDao(), db.pointsDao())
        // viewModelScope uses the main thread, so create the ViewModel there.
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            vm = ChoreViewModel(repository)
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun tickingChoreUpdatesState() = runBlocking {
        vm.addChore("  Brush teeth  ", 3)
        vm.addChore("", 5)          // ignored: blank title
        vm.addChore("Nothing", 0)   // ignored: zero points

        // first { condition } waits until the state matches (withTimeout stops it hanging forever).
        val withChore = withTimeout(5_000) { vm.uiState.first { it.chores.isNotEmpty() } }
        val chore = withChore.chores.single()
        assertEquals("Brush teeth", chore.title)

        vm.toggleChore(chore)
        // Balance and "done today" come from two separate queries that can update a few ms apart,
        // so wait until BOTH have caught up.
        val done = withTimeout(5_000) {
            vm.uiState.first { it.balance == 3 && chore.id in it.completedTodayIds }
        }
        assertEquals(setOf(chore.id), done.completedTodayIds)
    }

    @Test
    fun redeemWithoutPointsShowsMessage() = runBlocking {
        vm.redeemReward(Reward(title = "Ice cream", cost = 10))
        val msg = withTimeout(5_000) { vm.message.first { it != null } }
        assertEquals("Not enough points for Ice cream", msg)
    }
}
