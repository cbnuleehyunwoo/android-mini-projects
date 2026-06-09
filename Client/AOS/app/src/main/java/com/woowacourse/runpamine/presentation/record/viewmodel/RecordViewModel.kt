package com.woowacourse.runpamine.presentation.record.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.woowacourse.runpamine.domain.run.RunRecordRepository
import com.woowacourse.runpamine.domain.run.RunSession
import com.woowacourse.runpamine.presentation.record.model.RecordPeriod
import com.woowacourse.runpamine.presentation.record.model.RunningRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale

class RecordViewModel(
    private val runRecordRepository: RunRecordRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecordUiState(isLoading = true))
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    init {
        loadRecords()
    }

    fun selectPeriod(period: RecordPeriod) {
        if (_uiState.value.period == period) return
        _uiState.update { state ->
            state.copy(period = period)
        }
        loadRecords()
    }

    fun retry() {
        loadRecords()
    }

    private fun loadRecords() {
        val period = _uiState.value.period
        val anchorDate = _uiState.value.anchorDate

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                when (period) {
                    RecordPeriod.WEEK -> runRecordRepository.getWeeklyRuns(anchorDate)
                    RecordPeriod.MONTH -> runRecordRepository.getMonthlyRuns(YearMonth.from(anchorDate))
                }
            }.onSuccess { summary ->
                _uiState.update {
                    it.copy(
                        records = summary.runs.map { run -> run.toRunningRecord() },
                        recordedDates = summary.days.filter { day -> day.hasRun }.map { day -> day.date }.toSet(),
                        totalDistanceKm = summary.totalDistanceMeters / METERS_PER_KILOMETER,
                        isLoading = false,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "러닝 기록을 불러오지 못했어요.",
                    )
                }
            }
        }
    }

    class Factory(
        private val runRecordRepository: RunRecordRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(RecordViewModel::class.java))
            return RecordViewModel(runRecordRepository) as T
        }
    }
}

private fun RunSession.toRunningRecord(): RunningRecord =
    RunningRecord(
        id = id,
        date = startedAt.atZone(ZoneId.systemDefault()).toLocalDate(),
        distanceKm = distanceMeters / METERS_PER_KILOMETER,
        duration = durationSeconds.toDurationText(),
        pace = averagePaceSecondsPerKm.toPaceText(),
    )

private fun Long.toDurationText(): String {
    val hours = this / SECONDS_PER_HOUR
    val minutes = (this % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val seconds = this % SECONDS_PER_MINUTE
    return if (hours > 0) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}

private fun Int.toPaceText(): String {
    if (this <= 0) return "-'--\"/km"
    val minutes = this / SECONDS_PER_MINUTE
    val seconds = this % SECONDS_PER_MINUTE
    return String.format(Locale.getDefault(), "%d'%02d\"/km", minutes, seconds)
}

private const val METERS_PER_KILOMETER = 1_000.0
private const val SECONDS_PER_MINUTE = 60
private const val SECONDS_PER_HOUR = 3_600
