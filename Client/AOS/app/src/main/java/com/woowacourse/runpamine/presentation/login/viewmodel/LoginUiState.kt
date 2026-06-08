package com.woowacourse.runpamine.presentation.login.viewmodel

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null,
)
