package com.woowacourse.runpamine.presentation.team.components.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.presentation.team.model.TeamMember
import com.woowacourse.runpamine.ui.theme.Red40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun TeamContent(
    teamName: String,
    date: String,
    totalDistance: String,
    completedMemberCount: Int,
    totalMemberCount: Int,
    members: List<TeamMember>,
    onAddClick: () -> Unit,
    onLeaveTeamClick: () -> Unit,
    onPreviousDateClick: () -> Unit,
    onNextDateClick: () -> Unit,
    canMoveToNextDate: Boolean,
    isDateLoading: Boolean,
    isLeavingTeam: Boolean,
    onMemberClick: (TeamMember) -> Unit,
    modifier: Modifier = Modifier,
    memberErrorMessage: String? = null,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            TeamHeader(
                teamName = teamName,
                onAddClick = onAddClick,
                onLeaveTeamClick = onLeaveTeamClick,
                isLeavingTeam = isLeavingTeam,
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                IconButton(
                    onClick = onPreviousDateClick,
                    enabled = !isDateLoading,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "이전 날짜",
                    )
                }
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                )
                IconButton(
                    onClick = onNextDateClick,
                    enabled = canMoveToNextDate && !isDateLoading,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "다음 날짜",
                        tint = if (canMoveToNextDate) Color.Black else Color.Gray.copy(alpha = 0.3f),
                    )
                }
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TeamSummaryCard(
                    value = totalDistance,
                    label = "팀 총 거리",
                    modifier = Modifier.weight(1f),
                )
                TeamSummaryCard(
                    value = "$completedMemberCount / $totalMemberCount",
                    label = "완료 / 전체",
                    modifier = Modifier.weight(1f),
                )
            }
        }

        memberErrorMessage?.let { message ->
            item {
                Text(
                    text = message,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Red40,
                    textAlign = TextAlign.Center,
                )
            }
        }

        items(
            items = members,
            key = { it.id },
        ) { member ->
            TeamMemberCard(
                member = member,
                distance = member.distance,
                time = member.time,
                pace = member.pace,
                onClick = { onMemberClick(member) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TeamContentPreview() {
    RunpamineTheme {
        TeamContent(
            teamName = "볼트 멋쟁이",
            date = "2026년 6월 2일 - 화요일",
            totalDistance = "324 km",
            completedMemberCount = 3,
            totalMemberCount = 4,
            members =
                listOf(
                    TeamMember(
                        id = "1",
                        name = "커비커비커비커비커",
                        distance = "12.3 km",
                        time = "28:35",
                        pace = "2'19\"",
                        calories = "344",
                    ),
                    TeamMember(
                        id = "2",
                        name = "러너",
                        distance = "1.1 km",
                        time = "33:41",
                        pace = "30'37\"",
                        calories = "66",
                    ),
                ),
            onAddClick = {},
            onLeaveTeamClick = {},
            onPreviousDateClick = {},
            onNextDateClick = {},
            canMoveToNextDate = true,
            isDateLoading = false,
            isLeavingTeam = false,
            onMemberClick = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
