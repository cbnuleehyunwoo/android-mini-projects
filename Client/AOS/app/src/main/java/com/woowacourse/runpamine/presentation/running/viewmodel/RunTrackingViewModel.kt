package com.woowacourse.runpamine.presentation.running.viewmodel

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.woowacourse.runpamine.domain.run.RunSyncRepository
import com.woowacourse.runpamine.domain.run.RunTrackingRepository
import com.woowacourse.runpamine.service.RunTrackingService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

class RunTrackingViewModel(
    application: Application,
    private val runTrackingRepository: RunTrackingRepository,
    private val runSyncRepository: RunSyncRepository,
) : AndroidViewModel(application) {
    private val errorMessage = MutableStateFlow<String?>(null)

    val currentRunState =
        combine(
            runTrackingRepository.observeCurrentRun(),
            errorMessage,
            tickerFlow(),
        ) { session, error, _ ->
            RunTrackingUiState(
                session = session,
                elapsedSeconds = session.elapsedSeconds(),
                isRunning = session != null,
                lastErrorMessage = error,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RunTrackingUiState(),
        )

    fun startRun() {
        val context = getApplication<Application>()
        ContextCompat.startForegroundService(
            context,
            RunTrackingService.startIntent(context),
        )
    }

    fun stopRun(onStopped: () -> Unit = {}) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val finishedSession = runTrackingRepository.stopRun()
            context.stopService(Intent(context, RunTrackingService::class.java))

            if (finishedSession != null) {
                runSyncRepository
                    .syncRun(finishedSession.id)
                    .onFailure { errorMessage.value = it.message }
            }
            onStopped()
        }
    }

    class Factory(
        private val application: Application,
        private val runTrackingRepository: RunTrackingRepository,
        private val runSyncRepository: RunSyncRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(RunTrackingViewModel::class.java))
            return RunTrackingViewModel(
                application = application,
                runTrackingRepository = runTrackingRepository,
                runSyncRepository = runSyncRepository,
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

private fun com.woowacourse.runpamine.domain.run.RunSession?.elapsedSeconds(): Long {
    val session = this ?: return 0
    val runningElapsedSeconds = Duration.between(session.startedAt, Instant.now()).seconds.coerceAtLeast(0)
    return maxOf(session.durationSeconds, runningElapsedSeconds)
}

private const val TICK_INTERVAL_MILLIS = 1_000L
