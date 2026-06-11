package com.woowacourse.runpamine.presentation.mypage.viewmodel

data class MyPageUiState(
    val nickname: String = "",
    val isLoading: Boolean = true,
    val isLoggingOut: Boolean = false,
    val isLoggedOut: Boolean = false,
    val errorMessage: String? = null,
)
