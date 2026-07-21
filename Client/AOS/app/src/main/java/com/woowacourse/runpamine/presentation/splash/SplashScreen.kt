package com.woowacourse.runpamine.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.woowacourse.runpamine.di.runpamineContainer
import com.woowacourse.runpamine.presentation.component.RunpamineLogo
import kotlinx.coroutines.delay

private const val SPLASH_EXTRA_DELAY_MILLIS = 1000L

@Composable
fun SplashScreen(
    onSplashFinished: (SplashDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.runpamineContainer
    val viewModel: SplashViewModel =
        viewModel(
            factory =
                SplashViewModel.Factory(
                    authRepository = container.authRepository,
                    profileRepository = container.profileRepository,
                ),
        )
    val destination by viewModel.destination.collectAsStateWithLifecycle()

    LaunchedEffect(destination) {
        destination?.let {
            delay(SPLASH_EXTRA_DELAY_MILLIS)
            viewModel.onDestinationHandled()
            onSplashFinished(it)
        }
    }

    SplashContent(modifier = modifier)
}

@Composable
private fun SplashContent(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White),
        contentAlignment = Alignment.Center,
    ) {
        RunpamineLogo(
            characterSize = 230.dp,
            titleFontSize = 48.sp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    SplashContent()
}
