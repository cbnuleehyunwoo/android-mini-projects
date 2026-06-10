package com.woowacourse.runpamine.domain.ranking

data class RankingSeason(
    val id: String,
    val name: String,
    val year: Int,
    val month: Int,
    val elapsedDays: Int,
)
