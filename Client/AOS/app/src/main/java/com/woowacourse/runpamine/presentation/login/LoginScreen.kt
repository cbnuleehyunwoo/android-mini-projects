package com.woowacourse.runpamine.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.di.runpamineContainer
import com.woowacourse.runpamine.presentation.component.RunpamineLogo
import com.woowacourse.runpamine.presentation.login.viewmodel.LoginUiState
import com.woowacourse.runpamine.presentation.login.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit = {},
) {
    val context = LocalContext.current
    val container = context.runpamineContainer
    val viewModel: LoginViewModel =
        viewModel(
            factory =
                LoginViewModel.Factory(
                    authRepository = container.authRepository,
                    googleAuthCredentialDataSource = container.googleAuthCredentialDataSource,
                ),
        )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    LoginContent(
        uiState = uiState,
        onGoogleLoginClick = {
            viewModel.signInWithGoogle(context)
        },
        modifier = modifier,
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onGoogleLoginClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding((42.5).dp),
            verticalArrangement = Arrangement.spacedBy(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RunpamineLogo()

            Image(
                painter = painterResource(R.drawable.btn_google_login),
                contentDescription = "Google로 로그인",
                modifier =
                    Modifier
                        .alpha(if (uiState.isLoading) 0.6f else 1f)
                        .clickable(
                            enabled = !uiState.isLoading,
                            role = Role.Button,
                            onClick = onGoogleLoginClick,
                        ),
            )
            if (uiState.isLoading) {
                CircularProgressIndicator()
            }
            uiState.errorMessage?.let { message ->
                Text(text = message)
            }
        }
    }
}

@Preview
@Composable
private fun LoginScreenPreview() {
    LoginContent(
        uiState = LoginUiState(),
        onGoogleLoginClick = {},
    )
}
