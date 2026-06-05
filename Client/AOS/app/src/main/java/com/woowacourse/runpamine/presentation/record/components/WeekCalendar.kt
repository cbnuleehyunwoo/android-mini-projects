package com.woowacourse.runpamine.presentation.record.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.ui.theme.Gray40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme
import java.time.DayOfWeek
import java.time.LocalDate

private const val DAYS_IN_WEEK = 7
private const val DEFAULT_PAST_WEEKS = 52

@Composable
fun WeekCalendar(
    recordedDates: Set<LocalDate>,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    onDateClick: (LocalDate) -> Unit = {},
    pastWeeks: Int = DEFAULT_PAST_WEEKS,
) {
    // 오늘이 속한 주를 마지막 페이지로 두어 미래 주로는 넘어갈 수 없게 한다.
    val currentWeekStart = remember(today) { today.startOfWeek() }
    val pageCount = pastWeeks + 1
    val pagerState = rememberPagerState(initialPage = pageCount - 1) { pageCount }

    HorizontalPager(
        state = pagerState,
        modifier = modifier,
    ) { page ->
        val weeksFromCurrent = (pageCount - 1 - page).toLong()
        val weekStart = currentWeekStart.minusWeeks(weeksFromCurrent)
        WeekRow(
            weekStart = weekStart,
            recordedDates = recordedDates,
            today = today,
            onDateClick = onDateClick,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun WeekRow(
    weekStart: LocalDate,
    recordedDates: Set<LocalDate>,
    today: LocalDate,
    onDateClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(DAYS_IN_WEEK) { index ->
            val date = weekStart.plusDays(index.toLong())
            DayCell(
                date = date,
                isRecorded = date in recordedDates,
                isFuture = date.isAfter(today),
                onClick = { onDateClick(date) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isRecorded: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accent = MaterialTheme.colorScheme.primary
    val color =
        when {
            isFuture -> Gray40.copy(alpha = 0.3f)
            isRecorded -> accent
            else -> Gray40
        }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "${date.dayOfMonth} ${date.dayOfWeek.toKorean()}",
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
        Box(
            modifier =
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable(enabled = !isFuture, onClick = onClick),
        )
    }
}

// 주의 시작을 월요일로 맞춘다.
private fun LocalDate.startOfWeek(): LocalDate = minusDays((dayOfWeek.value - DayOfWeek.MONDAY.value).toLong())

private fun DayOfWeek.toKorean(): String =
    when (this) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun WeekCalendarPreview() {
    RunpamineTheme {
        val today = LocalDate.now()
        WeekCalendar(
            recordedDates = setOf(today, today.minusDays(2), today.minusDays(4)),
            today = today,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
