package com.woowacourse.runpamine.presentation.running.viewmodel

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.woowacourse.runpamine.domain.run.RunSession
import com.woowacourse.runpamine.domain.run.RunSyncRepository
import com.woowacourse.runpamine.domain.run.RunTrackingRepository
import com.woowacourse.runpamine.service.RunTrackingService
import com.woowacourse.runpamine.sound.RunAnnouncement
import com.woowacourse.runpamine.sound.RunAnnouncer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RunTrackingViewModel(
    application: Application,
    private val runTrackingRepository: RunTrackingRepository,
    private val runSyncRepository: RunSyncRepository,
    private val runAnnouncer: RunAnnouncer,
) : AndroidViewModel(application) {
    private val errorMessage = MutableStateFlow<String?>(null)

    init {
        runAnnouncer.prepare()
    }

    val currentRunState =
        combine(
            runTrackingRepository.observeCurrentRun(),
            runTrackingRepository.observeCurrentRoutePoints(),
            runTrackingRepository.observePaused(),
            errorMessage,
            tickerFlow(),
        ) { session, routePoints, isPaused, error, _ ->
            RunTrackingUiState(
                session = session,
                routePoints = routePoints,
                elapsedSeconds = runTrackingRepository.currentElapsedSeconds(),
                isRunning = session != null,
                isPaused = isPaused,
                lastErrorMessage = error,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RunTrackingUiState(),
        )

    fun startRun() {
        runAnnouncer.announce(RunAnnouncement.STARTED)
        val context = getApplication<Application>()
        ContextCompat.startForegroundService(
            context,
            RunTrackingService.startIntent(context),
        )
    }

    fun stopRun(onStopped: (RunSession?) -> Unit = {}) {
        runAnnouncer.announce(RunAnnouncement.ENDED)
        viewModelScope.launch {
            val context = getApplication<Application>()
            val finishedSession = runTrackingRepository.stopRun()
            context.stopService(Intent(context, RunTrackingService::class.java))

            onStopped(finishedSession)

            if (finishedSession != null) {
                runSyncRepository
                    .syncRun(finishedSession.id)
                    .onFailure { errorMessage.value = it.message }
            }
        }
    }

    fun togglePause() {
        viewModelScope.launch {
            if (currentRunState.value.isPaused) {
                runTrackingRepository.resumeRun()
                runAnnouncer.announce(RunAnnouncement.RESUMED)
            } else {
                runTrackingRepository.pauseRun()
                runAnnouncer.announce(RunAnnouncement.PAUSED)
            }
        }
    }

    class Factory(
        private val application: Application,
        private val runTrackingRepository: RunTrackingRepository,
        private val runSyncRepository: RunSyncRepository,
        private val runAnnouncer: RunAnnouncer,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(RunTrackingViewModel::class.java))
            return RunTrackingViewModel(
                application = application,
                runTrackingRepository = runTrackingRepository,
                runSyncRepository = runSyncRepository,
                runAnnouncer = runAnnouncer,
            ) as T
        }
    }
}

private fun tickerFlow(): Flow<Unit> =
    flow {
        while (true) {
            emit(Unit)
            delay(TICK_INTERVAL_MILLIS)
        }
    }

private const val TICK_INTERVAL_MILLIS = 1_000L
