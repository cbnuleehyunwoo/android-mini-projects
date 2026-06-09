package com.woowacourse.runpamine.domain.run

data class RunPeriodSummary(
    val totalDistanceMeters: Int,
    val days: List<RunDaySummary>,
    val runs: List<RunSession>,
)
