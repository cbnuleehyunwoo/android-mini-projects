package com.woowacourse.runpamine.presentation.record.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.woowacourse.runpamine.domain.run.RunRecordRepository
import com.woowacourse.runpamine.domain.run.RunSession
import com.woowacourse.runpamine.presentation.record.model.RecordPeriod
import com.woowacourse.runpamine.presentation.record.model.RunningRecord
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

class RecordViewModel(
    private val runRecordRepository: RunRecordRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RecordUiState(isLoading = true))
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()
    private var loadJob: Job? = null

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

    fun moveTo(anchorDate: LocalDate) {
        if (_uiState.value.anchorDate == anchorDate) return
        _uiState.update { state ->
            state.copy(anchorDate = anchorDate)
        }
        loadRecords()
    }

    fun retry() {
        loadRecords()
    }

    private fun loadRecords() {
        val period = _uiState.value.period
        val anchorDate = _uiState.value.anchorDate

        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                runCatching {
                    when (period) {
                        RecordPeriod.WEEK -> runRecordRepository.getWeeklyRuns(anchorDate)
                        RecordPeriod.MONTH -> runRecordRepository.getMonthlyRuns(YearMonth.from(anchorDate))
                    }
                }.onSuccess { summary ->
                    val visibleDays = summary.days.filter { day -> day.date in period.dateRange(anchorDate) }
                    val visibleRuns = summary.runs.filter { run -> run.localDate in period.dateRange(anchorDate) }
                    val totalDistanceMeters =
                        visibleDays
                            .takeIf { it.isNotEmpty() }
                            ?.sumOf { day -> day.distanceMeters }
                            ?: visibleRuns.sumOf { run -> run.distanceMeters }

                    _uiState.update {
                        it.copy(
                            records = visibleRuns.map { run -> run.toRunningRecord() },
                            recordedDates =
                                visibleDays
                                    .filter { day -> day.hasRun }
                                    .map { day -> day.date }
                                    .toSet() + visibleRuns.map { run -> run.localDate },
                            totalDistanceKm = totalDistanceMeters / METERS_PER_KILOMETER,
                            isLoading = false,
                        )
                    }

                    visibleRuns
                        .filter { run -> run.routePoints.size < MIN_ROUTE_POINT_COUNT }
                        .forEach { run -> loadRoutePoints(run.id) }
                }.onFailure { throwable ->
                    if (throwable is CancellationException) throw throwable

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "러닝 기록을 불러오지 못했어요.",
                        )
                    }
                }
            }
    }

    private suspend fun loadRoutePoints(runId: String) {
        val detail =
            try {
                runRecordRepository.getRunDetail(runId)
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
                return
            }
        if (detail.routePoints.size < MIN_ROUTE_POINT_COUNT) return

        _uiState.update { state ->
            state.copy(
                records =
                    state.records.map { record ->
                        if (record.id == runId) {
                            record.copy(routePoints = detail.routePoints)
                        } else {
                            record
                        }
                    },
            )
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

private fun RecordPeriod.dateRange(anchorDate: LocalDate): ClosedRange<LocalDate> =
    when (this) {
        RecordPeriod.WEEK -> {
            val start = anchorDate.startOfWeek()
            start..start.plusDays(6)
        }

        RecordPeriod.MONTH -> {
            val month = YearMonth.from(anchorDate)
            month.atDay(1)..month.atEndOfMonth()
        }
    }

private fun LocalDate.startOfWeek(): LocalDate = minusDays((dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())

private val RunSession.localDate: LocalDate
    get() = startedAt.atZone(ZoneId.systemDefault()).toLocalDate()

private fun RunSession.toRunningRecord(): RunningRecord =
    RunningRecord(
        id = id,
        date = localDate,
        dateText = localDate.toDisplayText(),
        startTime = startedAt.atZone(ZoneId.systemDefault()).toLocalTime().toTimeText(),
        endTime =
            endedAt
                ?.atZone(ZoneId.systemDefault())
                ?.toLocalTime()
                ?.toTimeText()
                .orEmpty(),
        distanceKm = distanceMeters / METERS_PER_KILOMETER,
        duration = durationSeconds.toDurationText(),
        pace = averagePaceSecondsPerKm.toPaceText(),
        calories = calories,
        routePoints = routePoints,
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

private fun LocalDate.toDisplayText(): String {
    val dayOfWeek = dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN)
    return String.format(Locale.getDefault(), "%d. %02d. %02d %s", year, monthValue, dayOfMonth, dayOfWeek)
}

private fun LocalTime.toTimeText(): String {
    val period = if (hour < HOURS_PER_HALF_DAY) "오전" else "오후"
    val displayHour = hour % HOURS_PER_HALF_DAY
    val hourText = if (displayHour == 0) HOURS_PER_HALF_DAY else displayHour
    return String.format(Locale.getDefault(), "%s %d:%02d", period, hourText, minute)
}

private const val METERS_PER_KILOMETER = 1_000.0
private const val SECONDS_PER_MINUTE = 60
private const val SECONDS_PER_HOUR = 3_600
private const val HOURS_PER_HALF_DAY = 12
private const val MIN_ROUTE_POINT_COUNT = 2
