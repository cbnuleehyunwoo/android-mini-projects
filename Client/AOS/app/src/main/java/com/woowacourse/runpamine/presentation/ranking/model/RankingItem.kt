package com.woowacourse.runpamine.presentation.ranking.model

data class RankingItem(
    val rank: Int?,
    val name: String,
    val valueText: String,
    val isMine: Boolean = false,
    val percentileText: String? = null,
) {
    val highlightText: String
        get() = listOfNotNull(valueText, percentileText?.let { "($it)" }).joinToString(" ")
}
