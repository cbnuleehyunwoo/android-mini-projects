package com.woowacourse.runpamine.presentation.team.components.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.presentation.team.model.TeamMember
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
    modifier: Modifier = Modifier,
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
            )
        }

        item {
            Text(
                text = date,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                textAlign = TextAlign.Center,
            )
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
        items(
            items = members,
            key = { it.id },
        ) { member ->
            TeamMemberCard(member = member)
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
                    TeamMember(id = 1L, name = "커비커비커비커비커", distance = "12.3", time = "28:35"),
                    TeamMember(id = 2L, name = "호이", distance = "1.1", time = "33:41"),
                    TeamMember(id = 3L, name = "볼트트", distance = "9.2", time = "30:30"),
                    TeamMember(id = 4L, name = "커비커비커비커비커", distance = "10.2", time = "31:58"),
                ),
            onAddClick = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
