package com.woowacourse.runpamine.presentation.running.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.domain.run.RunSession
import com.woowacourse.runpamine.ui.theme.RunpamineTheme
import java.time.Instant

@Composable
fun RunningScreenContent(
    session: RunSession?,
    elapsedSeconds: Long,
    isPaused: Boolean = false,
    modifier: Modifier = Modifier,
    onPauseClick: () -> Unit = {},
    onStopClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .safeDrawingPadding()
                    .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            RunningDistance(
                distance = session.distanceText(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            RunningTime(time = elapsedSeconds.elapsedTimeText())
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                RunningMetricCard(
                    title = stringResource(R.string.running_pace),
                    value = session.paceText(),
                    unit = "/km",
                    modifier = Modifier.weight(1f),
                )
                RunningMetricCard(
                    title = stringResource(R.string.running_kcal),
                    value = (session?.calories ?: 0).toString(),
                    unit = "kcal",
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            RunningControls(
                isPaused = isPaused,
                onPauseClick = onPauseClick,
                onStopClick = onStopClick,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(42.dp))
        }
    }
}

private fun RunSession?.distanceText(): String {
    val distanceKm = (this?.distanceMeters ?: 0) / 1_000.0
    return String.format("%.2f", distanceKm)
}

private fun Long.elapsedTimeText(): String {
    val minutes = this / SECONDS_PER_MINUTE
    val seconds = this % SECONDS_PER_MINUTE
    return "%02d:%02d".format(minutes, seconds)
}

private fun RunSession?.paceText(): String {
    val session = this ?: return "0'00\""
    if (session.averagePaceSecondsPerKm <= 0) return "0'00\""

    val paceSeconds = session.averagePaceSecondsPerKm.toLong()
    val minutes = paceSeconds / SECONDS_PER_MINUTE
    val seconds = paceSeconds % SECONDS_PER_MINUTE
    return "%d'%02d\"".format(minutes, seconds)
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun RunningScreenContentPreview() {
    RunpamineTheme {
        RunningScreenContent(
            session =
                RunSession(
                    id = "preview",
                    startedAt = Instant.parse("2026-06-08T00:00:00Z"),
                    distanceMeters = 5_200,
                    durationSeconds = 1_725,
                    averagePaceSecondsPerKm = 330,
                    calories = 505,
                ),
            elapsedSeconds = 1_725,
        )
    }
}

private const val SECONDS_PER_MINUTE = 60
