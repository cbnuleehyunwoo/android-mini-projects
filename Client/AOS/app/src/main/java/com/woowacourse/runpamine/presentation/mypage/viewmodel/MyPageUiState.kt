package com.woowacourse.runpamine.presentation.mypage.viewmodel

data class MyPageUiState(
    val nickname: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
