package com.woowacourse.runpamine.di

import android.content.Context
import androidx.room.Room
import com.google.android.gms.location.LocationServices
import com.woowacourse.runpamine.data.run.local.RoomRunLocalDataSource
import com.woowacourse.runpamine.data.run.local.RunDatabase
import com.woowacourse.runpamine.data.run.local.RunLocalDataSource
import com.woowacourse.runpamine.data.run.repository.DefaultRunTrackingRepository
import com.woowacourse.runpamine.data.run.repository.LocalOnlyRunSyncRepository
import com.woowacourse.runpamine.data.run.tracker.AndroidLocationTracker
import com.woowacourse.runpamine.domain.run.LocationTracker
import com.woowacourse.runpamine.domain.run.RunSyncRepository
import com.woowacourse.runpamine.domain.run.RunTrackingRepository

class RunpamineContainer(
    private val context: Context,
) {
    private val database: RunDatabase by lazy {
        Room
            .databaseBuilder(
                context.applicationContext,
                RunDatabase::class.java,
                "runpamine-runs.db",
            ).addMigrations(RunDatabase.MIGRATION_1_2)
            .build()
    }

    private val runLocalDataSource: RunLocalDataSource by lazy {
        RoomRunLocalDataSource(database.runDao())
    }

    private val locationTracker: LocationTracker by lazy {
        AndroidLocationTracker(
            LocationServices.getFusedLocationProviderClient(context.applicationContext),
        )
    }

    val runTrackingRepository: RunTrackingRepository by lazy {
        DefaultRunTrackingRepository(
            localDataSource = runLocalDataSource,
            locationTracker = locationTracker,
        )
    }

    val runSyncRepository: RunSyncRepository by lazy {
        LocalOnlyRunSyncRepository(runLocalDataSource)
    }
}
