package com.woowacourse.runpamine.domain.ranking

data class RankingPeriod(
    val type: String,
    val startsAt: String?,
    val endsAt: String?,
    val elapsedDays: Int,
)
