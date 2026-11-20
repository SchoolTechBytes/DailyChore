package com.nicholasnixon.dailychore.data.repository

import com.nicholasnixon.dailychore.data.dao.ChoreDao
import com.nicholasnixon.dailychore.data.dao.PointsDao
import com.nicholasnixon.dailychore.data.dao.RewardDao
import com.nicholasnixon.dailychore.data.entities.Chore
import com.nicholasnixon.dailychore.data.entities.PointsEntry
import com.nicholasnixon.dailychore.data.entities.Reward
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The single place the rest of the app goes for data.
 * The ViewModel (next stage) talks to this class, never to the DAOs directly,
 * so the app's rules (e.g. "you can't spend points you don't have") live in one place.
 */
class ChoreRepository(
    private val choreDao: ChoreDao,
    private val rewardDao: RewardDao,
    private val pointsDao: PointsDao
) {

    // --- Live data the screens will watch ---

    val chores: Flow<List<Chore>> = choreDao.getAll()
    val rewards: Flow<List<Reward>> = rewardDao.getAll()
    val balance: Flow<Int> = pointsDao.getBalance()

    /**
     * Ids of chores ticked off today. This is a function, not a val, so it uses the
     * date at the moment it's called. (A val would keep the date from app start.)
     */
    fun completedTodayIds(): Flow<List<Long>> = pointsDao.getCompletedChoreIds(today())

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

    // --- Kid: do chores and spend points ---

    /** Gives the chore's points for today. Does nothing if it was already done today. */
    suspend fun completeChore(chore: Chore) {
        val date = today()
        if (pointsDao.countCompletions(chore.id, date) > 0) return
        pointsDao.insert(
            PointsEntry(
                choreId = chore.id,
                description = chore.title,
                amount = chore.points,
                date = date
            )
        )
    }

    /** Un-ticks today's completion and takes its points back. */
    suspend fun undoChore(chore: Chore) {
        pointsDao.deleteCompletion(chore.id, today())
    }

    /**
     * Spends points on a reward.
     * @return true if it worked, false if the kid didn't have enough points.
     */
    suspend fun redeemReward(reward: Reward): Boolean {
        if (pointsDao.getBalanceNow() < reward.cost) return false
        pointsDao.insert(
            PointsEntry(
                choreId = null,
                description = "Redeemed: ${reward.title}",
                amount = -reward.cost,
                date = today()
            )
        )
        return true
    }

    // java.time.LocalDate needs Android 8 (API 26); our minSdk is 24, so we use SimpleDateFormat.
    private fun today(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}
