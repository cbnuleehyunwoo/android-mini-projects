package com.woowacourse.runpamine.shared.ui.screens.running

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.woowacourse.runpamine.shared.ui.model.GeoPointUi
import com.woowacourse.runpamine.shared.ui.model.RunRecordUi
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTheme

@Composable
fun RunDetailScreen(
    record: RunRecordUi,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    RunningSummaryScreen(
        record = record,
        onDone = onBack,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun RunDetailScreenPreview() {
    RunpamineTheme {
        RunDetailScreen(
            record =
                RunRecordUi(
                    id = "detail-preview",
                    dateText = "2026년 7월 16일 목요일",
                    distanceKm = 5.24,
                    durationText = "00:31:14",
                    paceText = "5'58\"",
                    calories = 304,
                    startTimeText = "오전 7:10",
                    endTimeText = "오전 7:41",
                    route =
                        listOf(
                            GeoPointUi(37.5665, 126.9780),
                            GeoPointUi(37.5683, 126.9815),
                            GeoPointUi(37.5654, 126.9842),
                            GeoPointUi(37.5626, 126.9818),
                        ),
                ),
            onBack = {},
        )
    }
}
