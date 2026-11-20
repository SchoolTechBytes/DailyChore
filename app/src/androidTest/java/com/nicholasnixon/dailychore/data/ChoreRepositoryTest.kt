package com.nicholasnixon.dailychore.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nicholasnixon.dailychore.data.database.AppDatabase
import com.nicholasnixon.dailychore.data.entities.Reward
import com.nicholasnixon.dailychore.data.repository.ChoreRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs on an emulator/device. Uses an in-memory database, so it never touches the real app data
 * and is thrown away after each test.
 */
@RunWith(AndroidJUnit4::class)
class ChoreRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: ChoreRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = ChoreRepository(db.choreDao(), db.rewardDao(), db.pointsDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    // runBlocking lets a normal test call suspend functions.
    @Test
    fun completingChoreAddsPointsOncePerDay() = runBlocking {
        repository.addChore("Make bed", 5)
        val chore = repository.chores.first().single()

        repository.completeChore(chore)
        repository.completeChore(chore) // second tick on the same day is ignored

        assertEquals(5, repository.balance.first())
        assertEquals(listOf(chore.id), repository.completedTodayIds().first())

        repository.undoChore(chore)
        assertEquals(0, repository.balance.first())
    }

    @Test
    fun redeemOnlyWorksWithEnoughPoints() = runBlocking {
        repository.addChore("Feed dog", 10)
        repository.completeChore(repository.chores.first().single())

        assertFalse(repository.redeemReward(Reward(title = "Movie night", cost = 50)))
        assertEquals(10, repository.balance.first())

        assertTrue(repository.redeemReward(Reward(title = "Sticker", cost = 4)))
        assertEquals(6, repository.balance.first())
    }
}
