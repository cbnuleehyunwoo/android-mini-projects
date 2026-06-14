package com.woowacourse.runpamine.presentation.record.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.presentation.record.model.RunningRecord
import com.woowacourse.runpamine.presentation.running.components.RunningRouteMap
import com.woowacourse.runpamine.ui.theme.Gray40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

@Composable
fun RecordItem(
    record: RunningRecord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
            ) {
                if (record.routePoints.isNotEmpty()) {
                    RunningRouteMap(
                        points = record.routePoints,
                        modifier = Modifier.fillMaxSize(),
                        isInteractive = false,
                        routePadding = MINI_MAP_ROUTE_PADDING,
                    )
                }
            }
            Column(
                modifier =
                    Modifier
                        .padding(start = 16.dp)
                        .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = record.date.toDisplayString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Gray40,
                )
                Text(
                    text = String.format(Locale.getDefault(), "%.1fKM", record.distanceKm),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconLabel(icon = Icons.Default.Schedule, text = record.duration)
                    IconLabel(icon = Icons.Default.Speed, text = record.pace)
                }
            }
        }
    }
}

@Composable
private fun IconLabel(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Gray40,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray40,
        )
    }
}

private fun LocalDate.toDisplayString(): String =
    String.format(
        Locale.getDefault(),
        "%d. %02d. %02d %s",
        year,
        monthValue,
        dayOfMonth,
        dayOfWeek.toKoreanFull(),
    )

private fun DayOfWeek.toKoreanFull(): String =
    when (this) {
        DayOfWeek.MONDAY -> "월요일"
        DayOfWeek.TUESDAY -> "화요일"
        DayOfWeek.WEDNESDAY -> "수요일"
        DayOfWeek.THURSDAY -> "목요일"
        DayOfWeek.FRIDAY -> "금요일"
        DayOfWeek.SATURDAY -> "토요일"
        DayOfWeek.SUNDAY -> "일요일"
    }

private const val MINI_MAP_ROUTE_PADDING = 24

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun RecordItemPreview() {
    RunpamineTheme {
        RecordItem(
            record =
                RunningRecord(
                    id = "1",
                    date = LocalDate.of(2026, 5, 29),
                    distanceKm = 5.0,
                    duration = "28:45",
                    pace = "5'45\"/km",
                    calories = 344,
                ),
            onClick = {},
        )
    }
}
