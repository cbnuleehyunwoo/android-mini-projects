package com.woowacourse.runpamine.presentation.running

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.domain.run.RunPoint
import com.woowacourse.runpamine.presentation.component.BottomButton
import com.woowacourse.runpamine.presentation.running.components.RunningMetricCard
import com.woowacourse.runpamine.presentation.running.components.RunningRouteMap
import com.woowacourse.runpamine.ui.theme.Gray40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun RunningCompleteScreen(
    distanceMeters: Int,
    distance: String,
    time: String,
    pace: String,
    calories: String,
    routePoints: List<RunPoint>,
    onCompleteClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 96.dp),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Outlined.IosShare,
                        contentDescription = "러닝 기록 공유",
                    )
                }
            }
            RunningRouteMap(
                points = routePoints,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(330.dp),
            )
            Spacer(modifier = Modifier.height(20.dp))
            Column(
                modifier = Modifier.padding(horizontal = 22.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RunningMetricCard(
                        title = stringResource(R.string.running_distance),
                        value = distance,
                        unit = stringResource(R.string.running_distance_unit),
                        modifier = Modifier.weight(1f),
                        summaryStyle = true,
                    )
                    RunningMetricCard(
                        title = stringResource(R.string.running_time),
                        value = time,
                        unit = "",
                        modifier = Modifier.weight(1f),
                        summaryStyle = true,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RunningMetricCard(
                        title = stringResource(R.string.running_pace),
                        value = pace,
                        unit = "/km",
                        modifier = Modifier.weight(1f),
                        summaryStyle = true,
                    )
                    RunningMetricCard(
                        title = stringResource(R.string.running_kcal),
                        value = calories,
                        unit = "kcal",
                        modifier = Modifier.weight(1f),
                        summaryStyle = true,
                    )
                }
                if (distanceMeters < MINIMUM_RECORDED_DISTANCE_METERS) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.img_error),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                        )
                        Text(
                            text = stringResource(R.string.running_short_run_recording_notice),
                            color = Gray40,
                            style =
                                MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                        )
                    }
                }
            }
        }
        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp)
        ) {
            BottomButton(
                text = "완료",
                onClick = onCompleteClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RunningCompleteScreenPreview() {
    RunpamineTheme {
        RunningCompleteScreen(
            distanceMeters = 12_300,
            distance = "12.3km",
            time = "23:32",
            pace = "5'30\"",
            calories = "344",
            routePoints = emptyList(),
            onCompleteClick = {},
            onShareClick = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ShortRunningCompleteScreenPreview() {
    RunpamineTheme {
        RunningCompleteScreen(
            distanceMeters = 99,
            distance = "0.10",
            time = "00:45",
            pace = "7'35\"",
            calories = "0",
            routePoints = emptyList(),
            onCompleteClick = {},
            onShareClick = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private const val MINIMUM_RECORDED_DISTANCE_METERS = 100
