package com.woowacourse.runpamine.presentation.running.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.Green40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RunningControls(
    isPaused: Boolean,
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = isPaused,
        modifier = modifier,
        transitionSpec = {
            (
                fadeIn(animationSpec = tween(RUNNING_CONTROL_ANIMATION_MILLIS)) +
                    scaleIn(
                        initialScale = RUNNING_CONTROL_INITIAL_SCALE,
                        animationSpec = tween(RUNNING_CONTROL_ANIMATION_MILLIS),
                    )
            ).togetherWith(
                fadeOut(animationSpec = tween(RUNNING_CONTROL_ANIMATION_MILLIS)) +
                    scaleOut(
                        targetScale = RUNNING_CONTROL_TARGET_SCALE,
                        animationSpec = tween(RUNNING_CONTROL_ANIMATION_MILLIS),
                    ),
            )
        },
        label = "runningControls",
    ) { paused ->
        if (paused) {
            PausedControls(
                onStopClick = onStopClick,
                onResumeClick = onPauseClick,
            )
        } else {
            RunningPauseControl(onPauseClick = onPauseClick)
        }
    }
}

@Composable
private fun RunningPauseControl(onPauseClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        IconControlButton(
            iconResId = R.drawable.ic_pause,
            contentDescription = stringResource(R.string.running_pause),
            containerColor = Color.Black,
            iconColor = Color.White,
            onClick = onPauseClick,
        )
    }
}

@Composable
private fun PausedControls(
    onStopClick: () -> Unit,
    onResumeClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StopControlButton(
            contentDescription = stringResource(R.string.running_stop),
            onClick = onStopClick,
        )
        IconControlButton(
            iconResId = R.drawable.ic_play,
            contentDescription = stringResource(R.string.running_resume),
            containerColor = Green40,
            iconColor = Color.Black,
            iconSize = 34.dp,
            onClick = onResumeClick,
        )
    }
}

@Composable
private fun IconControlButton(
    @DrawableRes iconResId: Int,
    contentDescription: String,
    containerColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = RUNNING_CONTROL_ICON_SIZE,
) {
    Box(
        modifier =
            modifier
                .size(RUNNING_CONTROL_BUTTON_SIZE)
                .clip(CircleShape)
                .background(containerColor)
                .clickable(
                    role = Role.Button,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(iconResId),
            contentDescription = contentDescription,
            colorFilter = ColorFilter.tint(iconColor),
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
private fun StopControlButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(RUNNING_CONTROL_BUTTON_SIZE)
                .clip(CircleShape)
                .background(Color.Black)
                .semantics {
                    this.contentDescription = contentDescription
                }.clickable(
                    role = Role.Button,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(RUNNING_STOP_ICON_SIZE)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White),
        )
    }
}

@Preview(showBackground = true, widthDp = 393)
@Composable
private fun RunningControlsPreview() {
    RunpamineTheme {
        RunningControls(
            isPaused = false,
            onPauseClick = {},
            onStopClick = {},
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 393)
@Composable
private fun PausedControlsPreview() {
    RunpamineTheme {
        RunningControls(
            isPaused = true,
            onPauseClick = {},
            onStopClick = {},
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        )
    }
}

private val RUNNING_CONTROL_BUTTON_SIZE = 70.dp
private val RUNNING_CONTROL_ICON_SIZE = 32.dp
private val RUNNING_STOP_ICON_SIZE = 24.dp
private const val RUNNING_CONTROL_ANIMATION_MILLIS = 220
private const val RUNNING_CONTROL_INITIAL_SCALE = 0.86f
private const val RUNNING_CONTROL_TARGET_SCALE = 0.92f
