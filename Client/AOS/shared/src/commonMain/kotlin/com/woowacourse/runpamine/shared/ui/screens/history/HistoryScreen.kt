package com.woowacourse.runpamine.shared.ui.screens.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.shared.generated.resources.Res
import com.woowacourse.runpamine.shared.generated.resources.icon_footprint
import com.woowacourse.runpamine.shared.generated.resources.icon_metric_pace
import com.woowacourse.runpamine.shared.generated.resources.icon_metric_time
import com.woowacourse.runpamine.shared.ui.model.GeoPointUi
import com.woowacourse.runpamine.shared.ui.model.HistoryPeriod
import com.woowacourse.runpamine.shared.ui.model.HistoryUiState
import com.woowacourse.runpamine.shared.ui.model.RunRecordUi
import com.woowacourse.runpamine.shared.ui.screens.running.RunRoutePreview
import com.woowacourse.runpamine.shared.ui.screens.running.formatDistanceKm
import com.woowacourse.runpamine.shared.ui.theme.RunpamineColors
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTheme
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTypography
import org.jetbrains.compose.resources.painterResource

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onPeriodSelected: (HistoryPeriod) -> Unit,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onRecordSelected: (RunRecordUi) -> Unit,
    modifier: Modifier = Modifier,
    canMoveNext: Boolean = true,
    onDateSelected: (String) -> Unit = {},
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().background(Color.White),
        contentPadding = PaddingValues(bottom = 92.dp),
    ) {
        item {
            HistoryHeader(
                period = state.period,
                onPeriodSelected = onPeriodSelected,
                modifier = Modifier.padding(horizontal = 30.dp, vertical = 34.dp),
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 30.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = formatDistanceKm(state.totalDistanceKm),
                    style =
                        RunpamineTypography.Header1.copy(
                            fontSize = 55.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    color = RunpamineColors.Primary,
                )
                Text(
                    text = "KM",
                    modifier = Modifier.padding(bottom = 9.dp),
                    style =
                        RunpamineTypography.Body1.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    color = HistoryColors.Unit,
                )
            }
        }
        item {
            HistoryCalendar(
                state = state,
                canMoveNext = canMoveNext,
                onPrevious = onPreviousPeriod,
                onNext = onNextPeriod,
                onDateSelected = onDateSelected,
                modifier = Modifier.padding(horizontal = 30.dp, vertical = 26.dp),
            )
        }

        when {
            state.isLoading -> {
                item {
                    HistoryLoadingState()
                }
            }
            state.records.isEmpty() -> {
                item {
                    HistoryEmptyState(period = state.period)
                }
            }
            else -> {
                items(state.records, key = RunRecordUi::id) { record ->
                    RunningRecordCard(
                        record = record,
                        onClick = { onRecordSelected(record) },
                        modifier = Modifier.padding(horizontal = 30.dp, vertical = 11.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryHeader(
    period: HistoryPeriod,
    onPeriodSelected: (HistoryPeriod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "기록",
            modifier = Modifier.weight(1f),
            style = RunpamineTypography.Header2,
            color = RunpamineColors.Primary,
        )
        Row(
            modifier =
                Modifier
                    .size(width = 150.dp, height = 42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(HistoryColors.PeriodBackground)
                    .padding(4.dp),
        ) {
            HistoryPeriod.entries.forEach { item ->
                val isSelected = item == period
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) RunpamineColors.Primary else Color.Transparent)
                            .clickable(role = Role.RadioButton) { onPeriodSelected(item) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (item == HistoryPeriod.Week) "주" else "월",
                        style = RunpamineTypography.Body2.copy(fontWeight = FontWeight.SemiBold),
                        color = if (isSelected) Color.White else HistoryColors.Unit,
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryCalendar(
    state: HistoryUiState,
    canMoveNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val calendarMonth = calendarMonth(state)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (state.period == HistoryPeriod.Week) 16.dp else 26.dp),
    ) {
        PeriodNavigator(
            title = state.periodTitle,
            canMoveNext = canMoveNext,
            onPrevious = onPrevious,
            onNext = onNext,
        )
        if (state.period == HistoryPeriod.Week) {
            WeekCalendar(
                selectedDate = state.selectedDate,
                datesWithRecords = state.datesWithRecords,
                onDateSelected = onDateSelected,
            )
        } else {
            MonthCalendar(
                year = calendarMonth.first,
                month = calendarMonth.second,
                selectedDate = state.selectedDate,
                datesWithRecords = state.datesWithRecords,
                onDateSelected = onDateSelected,
            )
        }
    }
}

@Composable
private fun PeriodNavigator(
    title: String,
    canMoveNext: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CalendarArrow(
            text = "‹",
            contentDescription = "이전 기간",
            enabled = true,
            onClick = onPrevious,
        )
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = RunpamineTypography.Title2.copy(fontWeight = FontWeight.Bold),
            color = RunpamineColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        CalendarArrow(
            text = "›",
            contentDescription = "다음 기간",
            enabled = canMoveNext,
            onClick = onNext,
        )
    }
}

@Composable
private fun CalendarArrow(
    text: String,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style =
                RunpamineTypography.Header2.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                ),
            color = if (enabled) Color.Black else RunpamineColors.Border,
        )
    }
}

@Composable
private fun WeekCalendar(
    selectedDate: String,
    datesWithRecords: Set<String>,
    onDateSelected: (String) -> Unit,
) {
    val selected =
        selectedDate.toCalendarDate()
            ?: datesWithRecords.firstNotNullOfOrNull(String::toCalendarDate)
            ?: CalendarDate(2026, 7, 16)
    val weekStart = selected.plusDays(-selected.mondayBasedWeekdayIndex())
    val recordDates = datesWithRecords.mapNotNull(String::toCalendarDate).map(CalendarDate::isoText).toSet()
    val weekdayLabels = listOf("월", "화", "수", "목", "금", "토", "일")

    Row(modifier = Modifier.fillMaxWidth()) {
        weekdayLabels.forEachIndexed { index, weekday ->
            val date = weekStart.plusDays(index)
            val hasRecord = date.isoText() in recordDates
            val isSelected = date == selected
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .clickable(role = Role.Button) {
                            onDateSelected(date.isoText())
                        },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "${date.day} $weekday",
                    style = RunpamineTypography.Caption1.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                    color = if (hasRecord || isSelected) RunpamineColors.Primary else HistoryColors.Unit,
                )
                Box(
                    modifier =
                        Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    hasRecord -> RunpamineColors.Primary
                                    isSelected -> RunpamineColors.Primary.copy(alpha = 0.18f)
                                    else -> HistoryColors.WeekDot
                                },
                            ),
                )
            }
        }
    }
}

@Composable
private fun MonthCalendar(
    year: Int,
    month: Int,
    selectedDate: String,
    datesWithRecords: Set<String>,
    onDateSelected: (String) -> Unit,
) {
    val recordDays = datesWithRecords.mapNotNull { it.dateNumbers().lastOrNull() }.toSet()
    val selectedDay = selectedDate.dateNumbers().lastOrNull()
    val offset = firstWeekdayOffset(year, month)
    val cells = List(offset) { null } + (1..daysInMonth(year, month)).map { it }
    val paddedCells = cells + List((7 - cells.size % 7) % 7) { null }
    val weekdays = listOf("일", "월", "화", "수", "목", "금", "토")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth()) {
            weekdays.forEachIndexed { index, weekday ->
                Text(
                    text = weekday,
                    modifier = Modifier.weight(1f),
                    style = RunpamineTypography.Body2.copy(fontWeight = FontWeight.Bold),
                    color =
                        when (index) {
                            0 -> RunpamineColors.Danger
                            6 -> RunpamineColors.Primary
                            else -> HistoryColors.Subtitle
                        },
                    textAlign = TextAlign.Center,
                )
            }
        }
        paddedCells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    if (day == null) {
                        Spacer(Modifier.weight(1f).height(48.dp))
                    } else {
                        MonthDay(
                            day = day,
                            hasRecord = day in recordDays,
                            isSelected = day == selectedDay,
                            onClick = {
                                onDateSelected(
                                    "${year.toString().padStart(4, '0')}-" +
                                        "${month.toString().padStart(2, '0')}-" +
                                        day.toString().padStart(2, '0'),
                                )
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthDay(
    day: Int,
    hasRecord: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.height(48.dp).clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(27.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) RunpamineColors.Primary.copy(alpha = 0.12f) else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = day.toString(),
                style = RunpamineTypography.Body1.copy(fontSize = 18.sp, fontWeight = FontWeight.Medium),
                color = if (isSelected) RunpamineColors.Primary else RunpamineColors.TextPrimary,
            )
        }
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (hasRecord) RunpamineColors.Primary else Color.Transparent),
        )
    }
}

@Composable
private fun RunningRecordCard(
    record: RunRecordUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(126.dp)
                .shadow(16.dp, RoundedCornerShape(14.dp), clip = false)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        RunRoutePreview(
            route = record.route,
            modifier = Modifier.size(96.dp),
            cornerRadius = 8,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = record.dateText,
                style = RunpamineTypography.Caption1,
                color = HistoryColors.Unit,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${formatDistanceKm(record.distanceKm)}KM",
                style =
                    RunpamineTypography.Title2.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                color = Color.Black,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                RecordMetric(
                    icon = {
                        Image(
                            painter = painterResource(Res.drawable.icon_metric_time),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    text = record.durationText,
                )
                RecordMetric(
                    icon = {
                        Image(
                            painter = painterResource(Res.drawable.icon_metric_pace),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                    text = "${record.paceText}/km",
                )
            }
        }
    }
}

@Composable
private fun RecordMetric(
    icon: @Composable () -> Unit,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        icon()
        Text(
            text = text,
            style = RunpamineTypography.Body2.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
            color = HistoryColors.Unit,
            maxLines = 1,
        )
    }
}

@Composable
private fun HistoryLoadingState() {
    Column(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(30.dp),
            color = RunpamineColors.Primary,
            strokeWidth = 3.dp,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "러닝 기록을 불러오는 중이에요",
            style = RunpamineTypography.Body2.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
            color = RunpamineColors.TextSecondary,
        )
    }
}

@Composable
private fun HistoryEmptyState(period: HistoryPeriod) {
    Column(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.icon_footprint),
            contentDescription = null,
            modifier = Modifier.size(34.dp),
            alpha = 0.45f,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (period == HistoryPeriod.Week) "이번 주 러닝 기록이 없어요" else "이번 달 러닝 기록이 없어요",
            style = RunpamineTypography.Body2.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
            color = RunpamineColors.TextSecondary,
        )
    }
}

private fun String.dateNumbers(): List<Int> =
    Regex("\\d+")
        .findAll(this)
        .mapNotNull { it.value.toIntOrNull() }
        .toList()

private data class CalendarDate(
    val year: Int,
    val month: Int,
    val day: Int,
) {
    fun plusDays(offset: Int): CalendarDate {
        var nextYear = year
        var nextMonth = month
        var nextDay = day + offset
        while (nextDay < 1) {
            nextMonth -= 1
            if (nextMonth < 1) {
                nextMonth = 12
                nextYear -= 1
            }
            nextDay += daysInMonth(nextYear, nextMonth)
        }
        while (nextDay > daysInMonth(nextYear, nextMonth)) {
            nextDay -= daysInMonth(nextYear, nextMonth)
            nextMonth += 1
            if (nextMonth > 12) {
                nextMonth = 1
                nextYear += 1
            }
        }
        return CalendarDate(nextYear, nextMonth, nextDay)
    }

    fun mondayBasedWeekdayIndex(): Int {
        val sundayBasedIndex = (firstWeekdayOffset(year, month) + day - 1) % 7
        return (sundayBasedIndex + 6) % 7
    }

    fun isoText(): String =
        "${year.toString().padStart(4, '0')}-" +
            "${month.toString().padStart(2, '0')}-" +
            day.toString().padStart(2, '0')
}

private fun String.toCalendarDate(): CalendarDate? {
    val numbers = dateNumbers()
    val year = numbers.getOrNull(0)?.takeIf { it >= 1 } ?: return null
    val month = numbers.getOrNull(1)?.takeIf { it in 1..12 } ?: return null
    val day = numbers.getOrNull(2)?.takeIf { it in 1..daysInMonth(year, month) } ?: return null
    return CalendarDate(year, month, day)
}

private fun calendarMonth(state: HistoryUiState): Pair<Int, Int> {
    val titleNumbers = state.periodTitle.dateNumbers()
    val selectedNumbers = state.selectedDate.dateNumbers()
    val numbers = if (titleNumbers.size >= 2) titleNumbers else selectedNumbers
    val year = numbers.getOrNull(0)?.takeIf { it >= 1000 } ?: 2026
    val month = numbers.getOrNull(1)?.takeIf { it in 1..12 } ?: 7
    return year to month
}

private fun daysInMonth(
    year: Int,
    month: Int,
): Int =
    when (month) {
        2 -> if (year % 400 == 0 || year % 4 == 0 && year % 100 != 0) 29 else 28
        4, 6, 9, 11 -> 30
        else -> 31
    }

private fun firstWeekdayOffset(
    year: Int,
    month: Int,
): Int {
    val adjustedMonth = if (month < 3) month + 12 else month
    val adjustedYear = if (month < 3) year - 1 else year
    val yearOfCentury = adjustedYear % 100
    val century = adjustedYear / 100
    val zeller =
        (1 + (13 * (adjustedMonth + 1)) / 5 + yearOfCentury + yearOfCentury / 4 + century / 4 + 5 * century) % 7
    return (zeller + 6) % 7
}

private object HistoryColors {
    val PeriodBackground = Color(0xFFF0F0F3)
    val Unit = Color(0xFF8B8B8B)
    val Subtitle = Color(0xFF94A3B8)
    val WeekDot = Color(0xFFB7B7B7)
}

private val HistoryPreviewRoute =
    listOf(
        GeoPointUi(37.5665, 126.9780),
        GeoPointUi(37.5683, 126.9815),
        GeoPointUi(37.5654, 126.9842),
        GeoPointUi(37.5626, 126.9818),
    )

@Preview
@Composable
private fun HistoryWeekPreview() {
    RunpamineTheme {
        HistoryScreen(
            state =
                HistoryUiState(
                    period = HistoryPeriod.Week,
                    periodTitle = "2026년 7월 13일 - 7월 19일",
                    totalDistanceKm = 8.4,
                    selectedDate = "2026-07-16",
                    datesWithRecords = setOf("2026-07-14", "2026-07-16"),
                    records =
                        listOf(
                            RunRecordUi(
                                id = "run-1",
                                dateText = "2026. 07. 16 목요일",
                                distanceKm = 5.24,
                                durationText = "00:31:14",
                                paceText = "5'58\"",
                                calories = 304,
                                route = HistoryPreviewRoute,
                            ),
                        ),
                ),
            onPeriodSelected = {},
            onPreviousPeriod = {},
            onNextPeriod = {},
            onRecordSelected = {},
        )
    }
}

@Preview
@Composable
private fun HistoryMonthPreview() {
    RunpamineTheme {
        HistoryScreen(
            state =
                HistoryUiState(
                    period = HistoryPeriod.Month,
                    periodTitle = "2026년 7월",
                    totalDistanceKm = 21.6,
                    selectedDate = "2026-07-16",
                    datesWithRecords = setOf("2026-07-04", "2026-07-14", "2026-07-16", "2026-07-28"),
                    records = emptyList(),
                ),
            onPeriodSelected = {},
            onPreviousPeriod = {},
            onNextPeriod = {},
            onRecordSelected = {},
        )
    }
}
