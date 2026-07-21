package com.woowacourse.runpamine.presentation.record.model

import com.woowacourse.runpamine.domain.run.RunPoint
import java.time.LocalDate

data class RunningRecord(
    val id: String,
    val date: LocalDate,
    val distanceKm: Double,
    val duration: String,
    val pace: String,
    val calories: Int,
    val dateText: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val routePoints: List<RunPoint> = emptyList(),
)
