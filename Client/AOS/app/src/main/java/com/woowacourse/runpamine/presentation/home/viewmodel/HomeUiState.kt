package com.woowacourse.runpamine.presentation.home.viewmodel

data class HomeUiState(
    val nickname: String = "",
    val teamName: String? = null,
    val teamMemberCount: Int = 0,
    val todayRunMemberCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
