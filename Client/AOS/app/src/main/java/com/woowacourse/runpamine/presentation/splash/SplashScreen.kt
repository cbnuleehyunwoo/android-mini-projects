package com.woowacourse.runpamine.presentation.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.woowacourse.runpamine.presentation.component.RunpamineLogo

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        RunpamineLogo()
    }
}

@Preview(showBackground = true)
@Composable
private fun SplashScreenPreview() {
    SplashScreen()
}
