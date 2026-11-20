package com.nicholasnixon.dailychore.data.repository

import com.nicholasnixon.dailychore.data.dao.ChoreDao
import com.nicholasnixon.dailychore.data.dao.PointsDao
import com.nicholasnixon.dailychore.data.dao.ResetDao
import com.nicholasnixon.dailychore.data.dao.RewardDao
import com.nicholasnixon.dailychore.data.entities.Chore
import com.nicholasnixon.dailychore.data.entities.PointsEntry
import com.nicholasnixon.dailychore.data.entities.Reward
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The single place the rest of the app goes for chore/reward/points data.
 * The ViewModel talks to this class, never to the DAOs directly,
 * so the app's rules (e.g. "you can't spend points you don't have") live in one place.
 *
 * @param clock gives the current time. The app uses the real clock; tests pass a fake one
 *              so they can pretend it's tomorrow.
 */
class ChoreRepository(
    private val choreDao: ChoreDao,
    private val rewardDao: RewardDao,
    private val pointsDao: PointsDao,
    private val resetDao: ResetDao,
    private val clock: () -> Date = { Date() }
) {

    /**
     * A Mutex lets only one coroutine at a time run the code inside withLock { }.
     * Without it, a fast double-tap could run "already done today?" twice before either tap
     * saves, giving double points (or spending points twice on a reward).
     */
    private val mutex = Mutex()

    // --- Live data the screens will watch ---

    val chores: Flow<List<Chore>> = choreDao.getAll()
    val rewards: Flow<List<Reward>> = rewardDao.getAll()
    val balance: Flow<Int> = pointsDao.getBalance()

    /** Ids of chores ticked off on [date] ("yyyy-MM-dd"). */
    fun completedIdsOn(date: String): Flow<List<Long>> = pointsDao.getCompletedChoreIds(date)

    /** Ids of chores ticked off today (date taken at the moment this is called). */
    fun completedTodayIds(): Flow<List<Long>> = completedIdsOn(today())

    // --- Parent: manage chores and rewards ---

    suspend fun addChore(title: String, points: Int) {
        choreDao.insert(Chore(title = title, points = points))
    }

    suspend fun updateChore(chore: Chore) = choreDao.update(chore)

    suspend fun deleteChore(chore: Chore) = choreDao.delete(chore)

    suspend fun addReward(title: String, cost: Int) {
        rewardDao.insert(Reward(title = title, cost = cost))
    }

    suspend fun updateReward(reward: Reward) = rewardDao.update(reward)

    suspend fun deleteReward(reward: Reward) = rewardDao.delete(reward)

    /** Deletes ALL chores, rewards and points (used by the PIN reset). */
    suspend fun eraseAll() {
        mutex.withLock { resetDao.eraseAll() }
    }

    // --- Kid: do chores and spend points ---

    /** Gives the chore's points for today. Does nothing if it was already done today. */
    suspend fun completeChore(chore: Chore) {
        mutex.withLock {
            val date = today()
            if (pointsDao.countCompletions(chore.id, date) == 0) {
                pointsDao.insert(
                    PointsEntry(
                        choreId = chore.id,
                        description = chore.title,
                        amount = chore.points,
                        date = date
                    )
                )
            }
        }
    }

    /** Un-ticks today's completion and takes its points back. */
    suspend fun undoChore(chore: Chore) {
        mutex.withLock { pointsDao.deleteCompletion(chore.id, today()) }
    }

    /**
     * Spends points on a reward.
     * @return true if it worked, false if the kid didn't have enough points.
     */
    suspend fun redeemReward(reward: Reward): Boolean = mutex.withLock {
        if (pointsDao.getBalanceNow() < reward.cost) {
            false
        } else {
            pointsDao.insert(
                PointsEntry(
                    choreId = null,
                    description = "Redeemed: ${reward.title}",
                    amount = -reward.cost,
                    date = today()
                )
            )
            true
        }
    }

    /**
     * Today's date as "yyyy-MM-dd". Public so the ViewModel can notice when the day changes.
     * (java.time.LocalDate needs Android 8 / API 26; our minSdk is 24, so we use SimpleDateFormat.)
     */
    fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(clock())
}
