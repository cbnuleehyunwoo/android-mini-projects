package com.woowacourse.runpamine.presentation.home.viewmodel

data class HomeUiState(
    val nickname: String = "",
    val teamName: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
