package com.woowacourse.runpamine.presentation.record.viewmodel

import com.woowacourse.runpamine.presentation.record.model.RecordPeriod
import com.woowacourse.runpamine.presentation.record.model.RunningRecord
import java.time.LocalDate

data class RecordUiState(
    val period: RecordPeriod = RecordPeriod.WEEK,
    val anchorDate: LocalDate = LocalDate.now(),
    val records: List<RunningRecord> = emptyList(),
    val recordedDates: Set<LocalDate> = emptySet(),
    val totalDistanceKm: Double = 0.0,
    val isLoading: Boolean = false,
    val isLoadingIndicatorVisible: Boolean = false,
    val errorMessage: String? = null,
)
