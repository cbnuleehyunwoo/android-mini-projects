package com.woowacourse.runpamine.presentation.record.model

import java.time.LocalDate
import com.woowacourse.runpamine.domain.run.RunPoint

data class RunningRecord(
    val id: String,
    val date: LocalDate,
    val distanceKm: Double,
    val duration: String,
    val pace: String,
    val calories: Int,
    val routePoints: List<RunPoint> = emptyList(),
)
