package com.woowacourse.runpamine.shared.ui.screens.running

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.shared.ui.components.PrimaryButton
import com.woowacourse.runpamine.shared.ui.model.GeoPointUi
import com.woowacourse.runpamine.shared.ui.model.RunRecordUi
import com.woowacourse.runpamine.shared.ui.model.RunningUiState
import com.woowacourse.runpamine.shared.ui.theme.RunpamineColors
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTheme
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTypography

@Composable
fun RunningSummaryScreen(
    record: RunRecordUi,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RunningSummaryContent(
        dateText = record.dateText,
        timeRangeText = listOf(record.startTimeText, record.endTimeText).filter(String::isNotBlank).joinToString(" ~ "),
        elapsedText = record.durationText,
        distanceKm = record.distanceKm,
        paceText = record.paceText,
        calories = record.calories,
        route = record.route,
        onDone = onDone,
        modifier = modifier,
    )
}

@Composable
fun RunningSummaryScreen(
    state: RunningUiState,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RunningSummaryContent(
        dateText = state.dateText,
        timeRangeText = state.timeRangeText,
        elapsedText = state.elapsedText,
        distanceKm = state.distanceKm,
        paceText = state.paceText,
        calories = state.calories,
        route = state.route,
        onDone = onDone,
        modifier = modifier,
    )
}

@Composable
private fun RunningSummaryContent(
    dateText: String,
    timeRangeText: String,
    elapsedText: String,
    distanceKm: Double,
    paceText: String,
    calories: Int,
    route: List<GeoPointUi>,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().background(Color.White)) {
        RunRoutePreview(
            route = route,
            modifier = Modifier.fillMaxWidth().height(330.dp),
        )

        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 28.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = dateText.ifBlank { "러닝 완료" },
                    style = RunpamineTypography.Title2.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black,
                )
                if (timeRangeText.isNotBlank()) {
                    Text(
                        text = timeRangeText,
                        style = RunpamineTypography.Body1.copy(fontWeight = FontWeight.Normal),
                        color = RunpamineColors.TextPrimary,
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryMetricCard(
                        title = "시간",
                        value = elapsedText,
                        suffix = "",
                        modifier = Modifier.weight(1f),
                    )
                    SummaryMetricCard(
                        title = "거리",
                        value = formatDistanceKm(distanceKm),
                        suffix = "km",
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryMetricCard(
                        title = "페이스",
                        value = paceText,
                        suffix = "/km",
                        modifier = Modifier.weight(1f),
                    )
                    SummaryMetricCard(
                        title = "칼로리",
                        value = calories.toString(),
                        suffix = "kcal",
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            PrimaryButton(
                title = "완료",
                onClick = onDone,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 34.dp),
            )
        }
    }
}

@Composable
private fun SummaryMetricCard(
    title: String,
    value: String,
    suffix: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .height(110.dp)
                .shadow(12.dp, RoundedCornerShape(10.dp), clip = false)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = RunpamineTypography.Caption1.copy(fontSize = 13.sp, fontWeight = FontWeight.Medium),
            color = RunpamineColors.TextPrimary,
        )
        Spacer(Modifier.height(10.dp))
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = value,
                style =
                    RunpamineTypography.Header1.copy(
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Black,
                    ),
                color = Color.Black,
                maxLines = 1,
            )
            if (suffix.isNotEmpty()) {
                Text(
                    text = suffix,
                    modifier = Modifier.padding(bottom = 3.dp),
                    style = RunpamineTypography.Caption1.copy(fontWeight = FontWeight.Black),
                    color = Color.Black,
                )
            }
        }
    }
}

private val PreviewRoute =
    listOf(
        GeoPointUi(37.5665, 126.9780),
        GeoPointUi(37.5683, 126.9815),
        GeoPointUi(37.5654, 126.9842),
        GeoPointUi(37.5626, 126.9818),
        GeoPointUi(37.5641, 126.9771),
    )

@Preview
@Composable
private fun RunningSummaryScreenPreview() {
    RunpamineTheme {
        RunningSummaryScreen(
            record =
                RunRecordUi(
                    id = "preview",
                    dateText = "2026년 7월 16일 목요일",
                    distanceKm = 5.24,
                    durationText = "00:31:14",
                    paceText = "5'58\"",
                    calories = 304,
                    startTimeText = "오전 7:10",
                    endTimeText = "오전 7:41",
                    route = PreviewRoute,
                ),
            onDone = {},
        )
    }
}
