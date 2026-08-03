package com.woowacourse.runpamine.presentation.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.woowacourse.runpamine.presentation.component.ScreenTopBar
import com.woowacourse.runpamine.presentation.running.components.RunningMetricCard
import com.woowacourse.runpamine.presentation.running.components.RunningRouteMap
import com.woowacourse.runpamine.presentation.share.RunShareData
import com.woowacourse.runpamine.presentation.share.RunShareFlow
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
    var showShareFlow by remember { mutableStateOf(false) }

    LaunchedEffect(runId) {
        if (runId.isBlank()) return@LaunchedEffect
        routePoints =
            runCatching {
                runRecordRepository.getRunDetail(runId).routePoints
            }.getOrDefault(emptyList())
    }

    HistoryContent(
        distance = distance,
        time = time,
        pace = pace,
        calories = calories,
        date = date,
        startTime = startTime,
        endTime = endTime,
        routePoints = routePoints,
        onBack = onBack,
        onShare = { showShareFlow = true },
        modifier = modifier,
    )

    if (showShareFlow) {
        RunShareFlow(
            data =
                RunShareData(
                    distance = distance,
                    time = time,
                    pace = pace,
                    calories = calories,
                    date = date,
                    routePoints = routePoints,
                ),
            onClose = { showShareFlow = false },
            onSaved = { showShareFlow = false },
        )
    }
}

@Composable
private fun HistoryContent(
    distance: String,
    time: String,
    pace: String,
    calories: String,
    date: String,
    startTime: String,
    endTime: String,
    routePoints: List<RunPoint>,
    onBack: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
    ) {
        val mapHeight = maxHeight / 3
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
        ) {
            RunningRouteMap(
                points = routePoints,
                maxZoom = HISTORY_MAP_MAX_ZOOM,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(mapHeight),
                shape =
                    RoundedCornerShape(
                        bottomStart = 18.dp,
                        bottomEnd = 18.dp,
                    ),
            )
            Spacer(
                modifier =
                    Modifier
                        .height(28.dp),
            )
            Column(
                modifier =
                    Modifier.padding(
                        start = 24.dp,
                        top = 24.dp,
                        end = 24.dp,
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
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
                Spacer(
                    modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars),
                )
            }
        }

        ScreenTopBar(
            title = null,
            onBackClick = onBack,
            modifier =
                Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp),
        )
        IconButton(
            onClick = onShare,
            modifier =
                Modifier
                    .align(androidx.compose.ui.Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.IosShare,
                contentDescription = "러닝 기록 공유",
                tint = Color.Black,
            )
        }
    }
}

private const val HISTORY_MAP_MAX_ZOOM = 18f

@Preview(
    showBackground = true,
    widthDp = 393,
    heightDp = 852,
)
@Composable
private fun HistoryScreenPreview() {
    RunpamineTheme {
        HistoryContent(
            distance = "12.3",
            time = "23:32",
            pace = "5'30\"",
            calories = "344",
            date = "2026. 06. 16 화요일",
            startTime = "20:10",
            endTime = "20:33",
            routePoints = emptyList(),
            onBack = {},
            onShare = {},
        )
    }
}
