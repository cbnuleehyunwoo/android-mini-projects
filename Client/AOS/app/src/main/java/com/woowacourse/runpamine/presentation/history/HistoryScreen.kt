package com.woowacourse.runpamine.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.di.runpamineContainer
import com.woowacourse.runpamine.domain.run.RunPoint
import com.woowacourse.runpamine.presentation.component.ScreenTopBar
import com.woowacourse.runpamine.presentation.running.components.RunningMetricCard
import com.woowacourse.runpamine.presentation.running.components.RunningRouteMap
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun HistoryScreen(
    runId: String,
    distance: String,
    time: String,
    pace: String,
    calories: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val runRecordRepository = LocalContext.current.runpamineContainer.runRecordRepository
    var routePoints by remember(runId) { mutableStateOf(emptyList<RunPoint>()) }

    LaunchedEffect(runId) {
        if (runId.isBlank()) return@LaunchedEffect
        routePoints =
            runCatching {
                runRecordRepository.getRunDetail(runId).routePoints
            }.getOrDefault(emptyList())
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            ScreenTopBar(
                title = stringResource(R.string.history_title),
                onBackClick = onBack,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RunningRouteMap(
                points = routePoints,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(330.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                RunningMetricCard(
                    iconResId = R.drawable.footprint,
                    title = stringResource(R.string.running_distance),
                    value = distance,
                    unit = stringResource(R.string.running_distance_unit),
                    modifier = Modifier.weight(1f),
                )
                RunningMetricCard(
                    iconResId = R.drawable.timer,
                    title = stringResource(R.string.running_time),
                    value = time,
                    unit = "",
                    modifier = Modifier.weight(1f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                RunningMetricCard(
                    iconResId = R.drawable.pace,
                    title = stringResource(R.string.running_pace),
                    value = pace,
                    unit = "/km",
                    modifier = Modifier.weight(1f),
                )
                RunningMetricCard(
                    iconResId = R.drawable.ic_kcal,
                    title = stringResource(R.string.running_kcal),
                    value = calories,
                    unit = "kcal",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HistoryScreenPreview() {
    RunpamineTheme {
        HistoryScreen(
            runId = "preview",
            distance = "12.3",
            time = "23:32",
            pace = "5'30\"",
            calories = "344",
            onBack = {},
        )
    }
}
