package com.woowacourse.runpamine.presentation.team

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.presentation.team.model.TeamMember
import com.woowacourse.runpamine.ui.theme.Pretendard
import com.woowacourse.runpamine.ui.theme.RunpamineTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamMemberSeasonBottomSheet(
    member: TeamMember,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = Color.Transparent,
        modifier = modifier,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        brush =
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF3A7BFA), Color(0xFF0055FF)),
                            ),
                    ).padding(horizontal = 28.dp, vertical = 28.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = member.name,
                    modifier = Modifier.weight(1f),
                    fontFamily = Pretendard,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "닫기",
                        tint = Color.White,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.65f),
                )
                Text(
                    text = " ${member.teamJoinedAt.toJoinedDateText()} 합류",
                    fontFamily = Pretendard,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.65f),
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SeasonMetric(
                    label = "총 거리 (km)",
                    value = member.seasonDistance,
                    modifier = Modifier.weight(1f),
                )
                SeasonMetric(
                    label = "총 시간",
                    value = member.seasonDuration,
                    modifier = Modifier.weight(1f),
                )
                SeasonMetric(
                    label = "총 러닝 횟수",
                    value = member.seasonRunCount.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SeasonMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            fontFamily = Pretendard,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.65f),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontFamily = Pretendard,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

private fun String.toJoinedDateText(): String =
    runCatching {
        val date = LocalDate.parse(substringBefore("T"))
        "${date.year}년 ${date.monthValue}월 ${date.dayOfMonth}일"
    }.getOrDefault(ifBlank { "가입일 정보 없음" })

@Preview(showBackground = true)
@Composable
private fun TeamMemberSeasonBottomSheetPreview() {
    RunpamineTheme {
        TeamMemberSeasonBottomSheet(
            member =
                TeamMember(
                    id = "1",
                    name = "커비커비커비커비커",
                    distance = "0.0 km",
                    time = "0:00",
                    pace = "-",
                    calories = "0",
                    teamJoinedAt = "2026-05-01",
                    seasonDistance = "87.3",
                    seasonDuration = "7:51:32",
                    seasonRunCount = 12,
                    seasonAveragePace = "5′24″",
                ),
            onDismissRequest = {},
        )
    }
}
