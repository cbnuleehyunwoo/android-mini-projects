package com.woowacourse.runpamine.presentation.record.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.ui.theme.Gray40
import com.woowacourse.runpamine.ui.theme.Red40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme
import java.time.LocalDate
import java.time.YearMonth

private const val DAYS_IN_WEEK = 7
private val WEEKDAYS = listOf("일", "월", "화", "수", "목", "금", "토")

@Composable
fun MonthCalendar(
    recordedDates: Set<LocalDate>,
    modifier: Modifier = Modifier,
    anchorMonth: YearMonth = YearMonth.from(LocalDate.now()),
    today: LocalDate = LocalDate.now(),
    onMonthChange: (YearMonth) -> Unit = {},
) {
    val currentMonth = remember(today) { YearMonth.from(today) }
    val displayedMonth =
        remember(anchorMonth, currentMonth) {
            if (anchorMonth.isAfter(currentMonth)) currentMonth else anchorMonth
        }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MonthHeader(
            month = displayedMonth,
            canGoNext = displayedMonth.isBefore(currentMonth),
            onPrevious = { onMonthChange(displayedMonth.minusMonths(1)) },
            onNext = { onMonthChange(displayedMonth.plusMonths(1)) },
        )
        WeekdayHeader()
        MonthGrid(
            month = displayedMonth,
            recordedDates = recordedDates,
            today = today,
        )
    }
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    canGoNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Default.KeyboardArrowLeft,
            contentDescription = "이전 달",
            tint = Color.Black,
            modifier =
                Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onPrevious)
                    .size(28.dp),
        )
        Text(
            text = "${month.year}년 ${month.monthValue}월",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        )
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "다음 달",
            tint = if (canGoNext) Color.Black else Gray40.copy(alpha = 0.3f),
            modifier =
                Modifier
                    .clip(CircleShape)
                    .clickable(enabled = canGoNext, onClick = onNext)
                    .size(28.dp),
        )
    }
}

@Composable
private fun WeekdayHeader(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth()) {
        WEEKDAYS.forEachIndexed { index, label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color =
                    when (index) {
                        0 -> Red40
                        DAYS_IN_WEEK - 1 -> MaterialTheme.colorScheme.primary
                        else -> Gray40
                    },
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    recordedDates: Set<LocalDate>,
    today: LocalDate,
    modifier: Modifier = Modifier,
) {
    val firstDay = month.atDay(1)
    // 일요일을 한 주의 시작으로 맞춘다. (일=0 ... 토=6)
    val leadingOffset = firstDay.dayOfWeek.value % DAYS_IN_WEEK
    val gridStart = firstDay.minusDays(leadingOffset.toLong())
    val weekCount = ((leadingOffset + month.lengthOfMonth() + DAYS_IN_WEEK - 1) / DAYS_IN_WEEK)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(weekCount) { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(DAYS_IN_WEEK) { dayOfWeek ->
                    val date = gridStart.plusDays((week * DAYS_IN_WEEK + dayOfWeek).toLong())
                    DayCell(
                        date = date,
                        isCurrentMonth = date.month == month.month,
                        isFuture = date.isAfter(today),
                        isRecorded = date in recordedDates,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isFuture: Boolean,
    isRecorded: Boolean,
    modifier: Modifier = Modifier,
) {
    val numberColor =
        if (!isCurrentMonth || isFuture) Gray40.copy(alpha = 0.4f) else Color.Black
    val showDot = isCurrentMonth && !isFuture && isRecorded

    Column(
        modifier = modifier.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = numberColor,
        )
        Box(
            modifier =
                Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (showDot) MaterialTheme.colorScheme.primary else Color.Transparent),
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun MonthCalendarPreview() {
    RunpamineTheme {
        val today = LocalDate.now()
        MonthCalendar(
            recordedDates = setOf(today, today.minusDays(3), today.minusDays(10)),
            today = today,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
