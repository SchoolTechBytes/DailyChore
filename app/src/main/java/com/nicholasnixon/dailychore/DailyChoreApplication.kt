package com.nicholasnixon.dailychore

import android.app.Application
import com.nicholasnixon.dailychore.data.database.AppDatabase
import com.nicholasnixon.dailychore.data.repository.ChoreRepository
import com.nicholasnixon.dailychore.data.repository.PinRepository

/**
 * Android creates this object once, before any screen, and keeps it for as long as the app runs.
 * That makes it a good home for things the whole app shares, like the database and repositories.
 * (Registered in AndroidManifest.xml with android:name=".DailyChoreApplication".)
 *
 * `by lazy` means each value is only created the first time something asks for it.
 */
class DailyChoreApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    val repository: ChoreRepository by lazy {
        ChoreRepository(
            database.choreDao(),
            database.rewardDao(),
            database.pointsDao(),
            database.resetDao()
        )
    }

    val pinRepository: PinRepository by lazy { PinRepository(this) }
}
