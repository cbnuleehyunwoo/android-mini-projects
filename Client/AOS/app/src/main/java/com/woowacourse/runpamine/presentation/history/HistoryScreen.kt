package com.woowacourse.runpamine.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.di.runpamineContainer
import com.woowacourse.runpamine.domain.run.RunPoint
import com.woowacourse.runpamine.presentation.component.BottomButton
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
    date: String,
    startTime: String,
    endTime: String,
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
                modifier =
                    Modifier
                        .background(Color.White)
                        .padding(12.dp),
            )
        },
        bottomBar = {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(Color.White),
            ) {
                BottomButton(
                    text = stringResource(R.string.set_nickname_button),
                    onClick = onBack,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp),
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(
                        start = 24.dp,
                        top = 24.dp,
                        end = 24.dp,
                        bottom = 0.dp,
                    ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RunningRouteMap(
                points = routePoints,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(330.dp),
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = date,
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp,
                        ),
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
                Text(
                    text = "$startTime ~ $endTime",
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 14.sp,
                        ),
                    color = Color(0xFF6B7280),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                RunningMetricCard(
                    title = stringResource(R.string.running_distance),
                    value = distance,
                    unit = stringResource(R.string.running_distance_unit),
                    modifier = Modifier.weight(1f),
                    shadowElevation = 2.dp,
                )
                RunningMetricCard(
                    title = stringResource(R.string.running_time),
                    value = time,
                    unit = "",
                    modifier = Modifier.weight(1f),
                    shadowElevation = 2.dp,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                RunningMetricCard(
                    title = stringResource(R.string.running_pace),
                    value = pace,
                    unit = "/km",
                    modifier = Modifier.weight(1f),
                    shadowElevation = 2.dp,
                )
                RunningMetricCard(
                    title = stringResource(R.string.running_kcal),
                    value = calories,
                    unit = "kcal",
                    modifier = Modifier.weight(1f),
                    shadowElevation = 2.dp,
                )
            }
            Spacer(
                modifier = Modifier.height(16.dp),
            )
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
            date = "2026. 06. 16 화요일",
            startTime = "20:10",
            endTime = "20:33",
            onBack = {},
        )
    }
}
