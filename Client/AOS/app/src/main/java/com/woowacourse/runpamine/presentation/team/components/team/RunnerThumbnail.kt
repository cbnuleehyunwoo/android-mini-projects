package com.woowacourse.runpamine.presentation.team.components.team

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.presentation.component.RunpamineLottie
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun RunnerThumbnail(
    hasRunRecord: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .clipToBounds(),
        contentAlignment = Alignment.Center,
    ) {
        RunpamineLottie(
            rawResId = if (hasRunRecord) R.raw.encho_lottie15 else R.raw.hamberger_1,
            modifier =
                Modifier
                    .fillMaxSize()
                    .then(
                        if (hasRunRecord) {
                            Modifier
                        } else {
                            Modifier.graphicsLayer {
                                transformOrigin = TransformOrigin(0f, 0f)
                                scaleX = 2.67f
                                scaleY = 2.67f
                            }
                        },
                    ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RunnerThumbnailPreview() {
    RunpamineTheme {
        RunnerThumbnail(hasRunRecord = true)
    }
}
