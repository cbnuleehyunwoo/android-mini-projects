package com.woowacourse.runpamine.presentation.component

import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun RunpamineLottie(
    @RawRes rawResId: Int,
    modifier: Modifier = Modifier,
    iterations: Int = LottieConstants.IterateForever,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val composition = rememberLottieComposition(LottieCompositionSpec.RawRes(rawResId))
    val progress =
        animateLottieCompositionAsState(
            composition = composition.value,
            iterations = iterations,
        )

    LottieAnimation(
        composition = composition.value,
        progress = { progress.value },
        modifier = modifier,
        contentScale = contentScale,
    )
}
