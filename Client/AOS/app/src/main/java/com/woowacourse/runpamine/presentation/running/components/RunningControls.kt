package com.woowacourse.runpamine.presentation.running.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.Green40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun RunningControls(
    isPaused: Boolean,
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RunningControlButton(
            text = stringResource(if (isPaused) R.string.running_resume else R.string.running_pause),
            iconResId = if (isPaused) R.drawable.ic_play else R.drawable.ic_pause,
            containerColor = if (isPaused) Green40 else Color.Black,
            contentColor = Color.White,
            onClick = onPauseClick,
            modifier = Modifier.weight(0.62f),
        )
        RunningControlButton(
            text = stringResource(R.string.running_stop),
            iconResId = R.drawable.ic_stop,
            containerColor = Color(0xFFFFF1F1),
            contentColor = Color(0xFFBA1A1A),
            borderColor = Color(0xFFFFB4AB),
            onClick = onStopClick,
            modifier = Modifier.weight(1f),
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
