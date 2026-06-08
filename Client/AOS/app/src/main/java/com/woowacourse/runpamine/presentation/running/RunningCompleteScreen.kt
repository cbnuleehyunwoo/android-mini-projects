package com.woowacourse.runpamine.presentation.running

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.presentation.component.BottomButton
import com.woowacourse.runpamine.presentation.running.components.RunningMetricCard
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun RunningCompleteScreen(
    distance: String ,
    time: String,
    pace: String,
    calories: String,
    onCompleteClick: () -> Unit,
    modifier: Modifier = Modifier,
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
                    .padding(horizontal = 24.dp, vertical = 28.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
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
            Spacer(modifier = Modifier.weight(1f))
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
            onCompleteClick = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
