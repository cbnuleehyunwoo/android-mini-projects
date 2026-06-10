package com.woowacourse.runpamine.presentation.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.woowacourse.runpamine.di.runpamineContainer
import com.woowacourse.runpamine.presentation.record.components.MonthCalendar
import com.woowacourse.runpamine.presentation.record.components.RecordDistance
import com.woowacourse.runpamine.presentation.record.components.RecordHeader
import com.woowacourse.runpamine.presentation.record.components.RecordItem
import com.woowacourse.runpamine.presentation.record.components.WeekCalendar
import com.woowacourse.runpamine.presentation.record.model.RecordPeriod
import com.woowacourse.runpamine.presentation.record.model.RunningRecord
import com.woowacourse.runpamine.presentation.record.viewmodel.RecordUiState
import com.woowacourse.runpamine.presentation.record.viewmodel.RecordViewModel
import com.woowacourse.runpamine.ui.theme.RunpamineTheme
import java.time.LocalDate

@Composable
fun RecordScreen(
    onRecordClick: (RunningRecord) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val container = context.runpamineContainer
    val viewModel: RecordViewModel =
        viewModel(
            factory =
                RecordViewModel.Factory(
                    runRecordRepository = container.runRecordRepository,
                ),
        )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RecordContent(
        uiState = uiState,
        onPeriodSelect = viewModel::selectPeriod,
        onRetryClick = viewModel::retry,
        onRecordClick = onRecordClick,
        modifier = modifier,
    )
}

@Composable
private fun RecordContent(
    uiState: RecordUiState,
    onPeriodSelect: (RecordPeriod) -> Unit,
    onRetryClick: () -> Unit,
    onRecordClick: (RunningRecord) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            RecordHeader(
                period = uiState.period,
                onPeriodSelect = onPeriodSelect,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            RecordDistance(distanceKm = uiState.totalDistanceKm)
        }
        item {
            when (uiState.period) {
                RecordPeriod.WEEK ->
                    WeekCalendar(
                        recordedDates = uiState.recordedDates,
                        today = uiState.anchorDate,
                        modifier = Modifier.fillMaxWidth(),
                    )

                RecordPeriod.MONTH ->
                    MonthCalendar(
                        recordedDates = uiState.recordedDates,
                        today = uiState.anchorDate,
                        modifier = Modifier.fillMaxWidth(),
                    )
            }
        }
        when {
            uiState.isLoading -> {
                item {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            uiState.errorMessage != null -> {
                item {
                    RecordMessage(
                        text = uiState.errorMessage,
                        actionText = "다시 시도",
                        onActionClick = onRetryClick,
                    )
                }
            }

            uiState.records.isEmpty() -> {
                item {
                    RecordMessage(text = "아직 러닝 기록이 없어요.")
                }
            }
        }
        items(uiState.records, key = { it.id }) { record ->
            RecordItem(
                record = record,
                onClick = { onRecordClick(record) },
            )
        }
    }
}

@Composable
private fun RecordMessage(
    text: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: () -> Unit = {},
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (actionText == null) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(onClick = onActionClick) {
                    Text(text = actionText)
                }
            }
        }
    }
}

private fun sampleRecords(today: LocalDate): List<RunningRecord> =
    listOf(
        RunningRecord("1", today, 5.0, "28:45", "5'45\"/km", 344),
        RunningRecord("2", today.minusDays(1), 12.4, "54:12", "4'22\"/km", 512),
        RunningRecord("3", today.minusDays(2), 5.0, "1:24:30", "4'38\"/km", 289),
        RunningRecord("4", today.minusDays(3), 5.0, "1:24:30", "4'38\"/km", 301),
    ).sortedByDescending { it.date }

@Preview(showBackground = true)
@Composable
private fun RecordScreenPreview() {
    RunpamineTheme {
        val today = remember { LocalDate.now() }
        val records = remember(today) { sampleRecords(today) }
        RecordContent(
            uiState =
                RecordUiState(
                    records = records,
                    recordedDates = records.map { it.date }.toSet(),
                    totalDistanceKm = records.sumOf { it.distanceKm },
                ),
            onPeriodSelect = {},
            onRetryClick = {},
            onRecordClick = {},
        )
    }
}
