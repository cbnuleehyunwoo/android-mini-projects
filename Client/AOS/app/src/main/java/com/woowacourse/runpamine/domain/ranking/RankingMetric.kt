package com.woowacourse.runpamine.domain.ranking

enum class RankingMetric {
    DISTANCE,
    PACE,
    CONSISTENCY,
}

val RankingMetric.teamStandardLabel: String
    get() =
        when (this) {
            RankingMetric.DISTANCE -> "팀 총 거리 기준"
            RankingMetric.PACE -> "팀 평균 페이스 기준"
            RankingMetric.CONSISTENCY -> "평균 활동일 기준"
        }
