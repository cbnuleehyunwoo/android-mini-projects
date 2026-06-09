package com.woowacourse.runpamine.domain.run

import java.time.LocalDate

data class RunDaySummary(
    val date: LocalDate,
    val distanceMeters: Int,
    val hasRun: Boolean,
)
