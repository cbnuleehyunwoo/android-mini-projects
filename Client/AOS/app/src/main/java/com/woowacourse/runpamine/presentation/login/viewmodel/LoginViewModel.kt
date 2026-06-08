package com.woowacourse.runpamine.presentation.login.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.woowacourse.runpamine.data.auth.google.GoogleAuthCredentialDataSource
import com.woowacourse.runpamine.domain.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val googleAuthCredentialDataSource: GoogleAuthCredentialDataSource,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                authRepository.getCurrentSession()
            }.onSuccess { session ->
                if (session != null) {
                    _uiState.update { it.copy(isLoggedIn = true) }
                }
            }

            authRepository.observeSession().collectLatest { session ->
                if (session != null) {
                    _uiState.update { it.copy(isLoggedIn = true) }
                }
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                )
            }

            runCatching {
                val credential = googleAuthCredentialDataSource.requestCredential(context)
                authRepository.signInWithGoogleIdToken(
                    idToken = credential.idToken,
                    nonce = credential.nonce,
                )
            }.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.toLoginMessage(),
                    )
                }
            }
        }
    }

    class Factory(
        private val authRepository: AuthRepository,
        private val googleAuthCredentialDataSource: GoogleAuthCredentialDataSource,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(LoginViewModel::class.java))
            return LoginViewModel(
                authRepository = authRepository,
                googleAuthCredentialDataSource = googleAuthCredentialDataSource,
            ) as T
        }
    }
}

private fun Throwable.toLoginMessage(): String {
    val detail = message.orEmpty()
    return when {
        "local.properties" in detail -> detail
        "Account reauth failed" in detail ->
            "Google 계정 인증에 실패했어요. 기기의 Google 계정을 다시 확인한 뒤 시도해 주세요."
        else -> "Google 로그인에 실패했어요. 잠시 후 다시 시도해 주세요."
    }
}
