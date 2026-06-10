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
import com.woowacourse.runpamine.presentation.team.model.RunningStatus
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun RunnerThumbnail(
    runningStatus: RunningStatus,
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
            rawResId = runningStatus.rawResId,
            speed = runningStatus.speed,
            modifier =
                Modifier
                    .fillMaxSize()
                    .then(
                        if (runningStatus == RunningStatus.LongResting) {
                            Modifier.graphicsLayer {
                                transformOrigin = TransformOrigin(0f, 0f)
                                scaleX = 2.67f
                                scaleY = 2.67f
                            }
                        } else {
                            Modifier
                        },
                    ),
        )
    }
}

private val RunningStatus.rawResId: Int
    get() =
        when (this) {
            RunningStatus.LongResting -> R.raw.hamberger_1
            RunningStatus.Resting -> R.raw.no_run
            RunningStatus.Running -> R.raw.encho_lottie10
            RunningStatus.ThreeDayRunning -> R.raw.reverse
            RunningStatus.FiveDayRunning -> R.raw.cheeta
        }

private val RunningStatus.speed: Float
    get() =
        when (this) {
            RunningStatus.ThreeDayRunning -> 0.75f
            RunningStatus.FiveDayRunning -> 1.5f
            else -> 1f
        }

@Preview(showBackground = true)
@Composable
private fun RunnerThumbnailPreview() {
    RunpamineTheme {
        RunnerThumbnail(runningStatus = RunningStatus.Running)
    }
}
