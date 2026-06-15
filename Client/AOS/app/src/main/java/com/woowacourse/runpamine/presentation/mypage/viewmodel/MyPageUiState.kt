package com.woowacourse.runpamine.presentation.mypage.viewmodel

data class MyPageUiState(
    val nickname: String = "",
    val isLoading: Boolean = false,
    val isLoggingOut: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val isLoggedOut: Boolean = false,
    val errorMessage: String? = null,
)
