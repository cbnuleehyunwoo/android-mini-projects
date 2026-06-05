package com.woowacourse.runpamine.presentation.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.presentation.record.components.MonthCalendar
import com.woowacourse.runpamine.presentation.record.components.RecordDistance
import com.woowacourse.runpamine.presentation.record.components.RecordHeader
import com.woowacourse.runpamine.presentation.record.components.RecordItem
import com.woowacourse.runpamine.presentation.record.components.WeekCalendar
import com.woowacourse.runpamine.presentation.record.model.RecordPeriod
import com.woowacourse.runpamine.presentation.record.model.RunningRecord
import com.woowacourse.runpamine.ui.theme.RunpamineTheme
import java.time.LocalDate

@Composable
fun RecordScreen(modifier: Modifier = Modifier) {
    val today = remember { LocalDate.now() }
    var period by remember { mutableStateOf(RecordPeriod.WEEK) }
    val records = remember(today) { sampleRecords(today) }
    val recordedDates = remember(records) { records.map { it.date }.toSet() }
    val totalDistance = remember(records) { records.sumOf { it.distanceKm } }

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
                period = period,
                onPeriodSelect = { period = it },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            RecordDistance(distanceKm = totalDistance)
        }
        item {
            when (period) {
                RecordPeriod.WEEK ->
                    WeekCalendar(
                        recordedDates = recordedDates,
                        today = today,
                        modifier = Modifier.fillMaxWidth(),
                    )

                RecordPeriod.MONTH ->
                    MonthCalendar(
                        recordedDates = recordedDates,
                        today = today,
                        modifier = Modifier.fillMaxWidth(),
                    )
            }
        }
        items(records, key = { it.id }) { record ->
            RecordItem(record = record)
        }
    }
}

private fun sampleRecords(today: LocalDate): List<RunningRecord> =
    listOf(
        RunningRecord(1, today, 5.0, "28:45", "5'45\"/km"),
        RunningRecord(2, today.minusDays(1), 12.4, "54:12", "4'22\"/km"),
        RunningRecord(3, today.minusDays(2), 5.0, "1:24:30", "4'38\"/km"),
        RunningRecord(4, today.minusDays(3), 5.0, "1:24:30", "4'38\"/km"),
    ).sortedByDescending { it.date }

@Preview(showBackground = true)
@Composable
private fun RecordScreenPreview() {
    RunpamineTheme {
        RecordScreen()
    }
}
