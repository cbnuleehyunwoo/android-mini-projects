package com.woowacourse.runpamine.di

import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.google.android.gms.location.LocationServices
import com.woowacourse.runpamine.BuildConfig
import com.woowacourse.runpamine.data.auth.google.AndroidGoogleAuthCredentialDataSource
import com.woowacourse.runpamine.data.auth.google.GoogleAuthCredentialDataSource
import com.woowacourse.runpamine.data.auth.remote.ApiAuthRemoteDataSource
import com.woowacourse.runpamine.data.auth.remote.AuthRemoteDataSource
import com.woowacourse.runpamine.data.auth.repository.DefaultAuthRepository
import com.woowacourse.runpamine.data.auth.repository.MissingAuthConfigurationRepository
import com.woowacourse.runpamine.data.auth.storage.EncryptedAuthSessionStore
import com.woowacourse.runpamine.data.profile.remote.ApiProfileRemoteDataSource
import com.woowacourse.runpamine.data.profile.remote.ProfileRemoteDataSource
import com.woowacourse.runpamine.data.profile.repository.DefaultProfileRepository
import com.woowacourse.runpamine.data.ranking.remote.ApiRankingRemoteDataSource
import com.woowacourse.runpamine.data.ranking.remote.RankingRemoteDataSource
import com.woowacourse.runpamine.data.ranking.repository.DefaultRankingRepository
import com.woowacourse.runpamine.data.run.local.RoomRunLocalDataSource
import com.woowacourse.runpamine.data.run.local.RunDatabase
import com.woowacourse.runpamine.data.run.local.RunLocalDataSource
import com.woowacourse.runpamine.data.run.remote.ApiRunRemoteDataSource
import com.woowacourse.runpamine.data.run.remote.RunRemoteDataSource
import com.woowacourse.runpamine.data.run.repository.DefaultRunRecordRepository
import com.woowacourse.runpamine.data.run.repository.DefaultRunSyncRepository
import com.woowacourse.runpamine.data.run.repository.DefaultRunTrackingRepository
import com.woowacourse.runpamine.data.run.tracker.AndroidLocationTracker
import com.woowacourse.runpamine.data.team.remote.ApiTeamRemoteDataSource
import com.woowacourse.runpamine.data.team.remote.TeamRemoteDataSource
import com.woowacourse.runpamine.data.team.repository.DefaultTeamRepository
import com.woowacourse.runpamine.domain.auth.AuthRepository
import com.woowacourse.runpamine.domain.profile.ProfileRepository
import com.woowacourse.runpamine.domain.ranking.RankingRepository
import com.woowacourse.runpamine.domain.run.LocationTracker
import com.woowacourse.runpamine.domain.run.RunRecordRepository
import com.woowacourse.runpamine.domain.run.RunSyncRepository
import com.woowacourse.runpamine.domain.run.RunTrackingRepository
import com.woowacourse.runpamine.domain.team.TeamRepository
import com.woowacourse.runpamine.presentation.cache.RankingCache
import com.woowacourse.runpamine.presentation.cache.RecordCache
import com.woowacourse.runpamine.presentation.cache.TeamDashboardCache
import com.woowacourse.runpamine.service.RunTrackingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RunpamineContainer(
    private val context: Context,
) {
    val teamDashboardCache = TeamDashboardCache()
    val rankingCache = RankingCache()
    val recordCache = RecordCache()

    fun clearMainTabCaches() {
        teamDashboardCache.clear()
        rankingCache.clear()
        recordCache.clear()
    }

    suspend fun clearLocalUserData() {
        context.stopService(Intent(context, RunTrackingService::class.java))
        runTrackingRepository.discardActiveRun()
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
        clearMainTabCaches()
    }

    private val authSessionStore by lazy {
        EncryptedAuthSessionStore(context)
    }

    private val authRemoteDataSource: AuthRemoteDataSource by lazy {
        ApiAuthRemoteDataSource(
            baseUrl = BuildConfig.BASE_URL,
            sessionStore = authSessionStore,
        )
    }

    val googleAuthCredentialDataSource: GoogleAuthCredentialDataSource by lazy {
        AndroidGoogleAuthCredentialDataSource(
            webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID,
        )
    }

    val authRepository: AuthRepository by lazy {
        val missingKeys =
            buildList {
                if (BuildConfig.BASE_URL.isBlank()) add("base_url")
            }

        if (missingKeys.isEmpty()) {
            DefaultAuthRepository(authRemoteDataSource)
        } else {
            MissingAuthConfigurationRepository(missingKeys)
        }
    }

    private val profileRemoteDataSource: ProfileRemoteDataSource by lazy {
        ApiProfileRemoteDataSource(BuildConfig.BASE_URL)
    }

    val profileRepository: ProfileRepository by lazy {
        DefaultProfileRepository(
            authRepository = authRepository,
            remoteDataSource = profileRemoteDataSource,
        )
    }

    private val rankingRemoteDataSource: RankingRemoteDataSource by lazy {
        ApiRankingRemoteDataSource(BuildConfig.BASE_URL)
    }

    val rankingRepository: RankingRepository by lazy {
        DefaultRankingRepository(
            authRepository = authRepository,
            remoteDataSource = rankingRemoteDataSource,
        )
    }

    private val teamRemoteDataSource: TeamRemoteDataSource by lazy {
        ApiTeamRemoteDataSource(BuildConfig.BASE_URL)
    }

    val teamRepository: TeamRepository by lazy {
        DefaultTeamRepository(
            authRepository = authRepository,
            remoteDataSource = teamRemoteDataSource,
        )
    }

    private val database: RunDatabase by lazy {
        Room
            .databaseBuilder(
                context.applicationContext,
                RunDatabase::class.java,
                "runpamine-runs.db",
            ).addMigrations(
                RunDatabase.MIGRATION_1_2,
                RunDatabase.MIGRATION_2_3,
                RunDatabase.MIGRATION_3_4,
            ).build()
    }

    private val runLocalDataSource: RunLocalDataSource by lazy {
        RoomRunLocalDataSource(database.runDao())
    }

    private val runRemoteDataSource: RunRemoteDataSource by lazy {
        ApiRunRemoteDataSource(BuildConfig.BASE_URL)
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
            currentUserId = { authSessionStore.current()?.user?.id },
        )
    }

    val runRecordRepository: RunRecordRepository by lazy {
        DefaultRunRecordRepository(
            authRepository = authRepository,
            remoteDataSource = runRemoteDataSource,
        )
    }

    val runSyncRepository: RunSyncRepository by lazy {
        DefaultRunSyncRepository(
            authRepository = authRepository,
            localDataSource = runLocalDataSource,
            remoteDataSource = runRemoteDataSource,
        )
    }
}
