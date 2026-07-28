package com.woowacourse.runpamine.presentation.splash

import com.woowacourse.runpamine.domain.profile.UserProfile

sealed interface SplashUiState {
    data object Loading : SplashUiState

    data object ProfileLoadFailed : SplashUiState

    data class Completed(
        val destination: SplashDestination,
    ) : SplashUiState
}

internal fun Result<UserProfile?>.toSplashUiState(): SplashUiState =
    fold(
        onSuccess = { profile ->
            SplashUiState.Completed(
                if (profile == null) {
                    SplashDestination.TERMS_AGREEMENT
                } else {
                    SplashDestination.HOME
                },
            )
        },
        onFailure = {
            SplashUiState.ProfileLoadFailed
        },
    )
