package com.woowacourse.runpamine.presentation.join

data class JoinUiState(
    val code: String = "",
    val isLoading: Boolean = false,
    val isJoined: Boolean = false,
    val errorMessage: String? = null,
)
