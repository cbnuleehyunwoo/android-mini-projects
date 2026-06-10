package com.woowacourse.runpamine.presentation.record.model

import java.time.LocalDate

data class RunningRecord(
    val id: String,
    val date: LocalDate,
    val distanceKm: Double,
    val duration: String,
    val pace: String,
    val calories: Int,
)
