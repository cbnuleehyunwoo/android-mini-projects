package com.woowacourse.runpamine.presentation.running

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.domain.run.RunPoint
import com.woowacourse.runpamine.presentation.component.BottomButton
import com.woowacourse.runpamine.presentation.running.components.RunningMetricCard
import com.woowacourse.runpamine.presentation.running.components.RunningRouteMap
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun RunningCompleteScreen(
    distance: String,
    time: String,
    pace: String,
    calories: String,
    routePoints: List<RunPoint>,
    onCompleteClick: () -> Unit,
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
                    .padding(top = 28.dp)
                    .padding(bottom = 96.dp),
        ) {
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
            }
        }
        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 34.dp)
                    .navigationBarsPadding(),
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
            distance = "12.3km",
            time = "23:32",
            pace = "5'30\"",
            calories = "344",
            routePoints = emptyList(),
            onCompleteClick = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
