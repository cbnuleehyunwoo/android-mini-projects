package com.woowacourse.runpamine.presentation.login.viewmodel

data class LoginUiState(
    val isLoading: Boolean = false,
    val destination: LoginDestination? = null,
    val errorMessage: String? = null,
)

enum class LoginDestination {
    NICKNAME,
    HOME,
}
