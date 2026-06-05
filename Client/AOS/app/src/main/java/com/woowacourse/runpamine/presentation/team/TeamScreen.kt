package com.woowacourse.runpamine.presentation.team

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.Blue40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun TeamScreen(
    onInviteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val members =
        listOf(
            TeamMember(name = "커비커비커비커비커", distance = "12.3", time = "28:35"),
            TeamMember(name = "호이", distance = "1.1", time = "33:41"),
            TeamMember(name = "볼트트", distance = "9.2", time = "30:30"),
            TeamMember(name = "커비커비커비커비커", distance = "10.2", time = "31:58"),
        )

    TeamContent(
        teamName = "볼트 멋쟁이",
        date = "2026년 6월 2일 - 화요일",
        totalDistance = "324 km",
        completedMemberCount = 3,
        totalMemberCount = 4,
        members = members,
        onAddClick = onInviteClick,
        modifier =
            modifier
                .fillMaxSize()
                .safeDrawingPadding(),
    )
}

@Composable
private fun TeamContent(
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
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
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
        items(members) { member ->
            TeamMemberCard(member = member)
        }
    }
}

@Composable
private fun TeamHeader(
    teamName: String,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = teamName,
            modifier = Modifier.weight(1f),
            style =
                MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    lineHeight = 36.sp,
                ),
            fontWeight = FontWeight.Black,
            color = Blue40,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        IconButton(onClick = onAddClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "팀원 추가",
                tint = Blue40,
                modifier = Modifier.size(40.dp),
            )
        }
    }
}

@Composable
private fun TeamSummaryCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .height(72.dp)
                .border(
                    width = 1.2.dp,
                    color = Blue40,
                    shape = RoundedCornerShape(14.dp),
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = Blue40,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6F7B91),
        )
    }
}

@Composable
private fun TeamMemberCard(
    member: TeamMember,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .height(168.dp)
                .border(
                    width = 1.2.dp,
                    color = Blue40,
                    shape = RoundedCornerShape(18.dp),
                ).padding(12.dp),
    ) {
        Text(
            text = member.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = Color.Black,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RunnerThumbnail(
                modifier = Modifier.size(92.dp),
            )
            Spacer(modifier = Modifier.width(28.dp))
            DistanceText(
                distance = member.distance,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = member.time,
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp),
                fontWeight = FontWeight.Black,
                color = Color.Black,
            )
        }
    }
}

@Composable
private fun DistanceText(
    distance: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = distance,
            style =
                MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 30.sp,
                    lineHeight = 48.sp,
                ),
            fontWeight = FontWeight.Black,
            color = Color.Black,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "km",
            modifier = Modifier.padding(bottom = 5.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = Color(0xFF666666),
        )
    }
}

@Composable
private fun RunnerThumbnail(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .aspectRatio(1f)
                .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_normal_run),
            contentDescription = "러닝 이미지",
            modifier = Modifier.size(80.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

private data class TeamMember(
    val name: String,
    val distance: String,
    val time: String,
)

@Preview(showBackground = true)
@Composable
private fun TeamScreenPreview() {
    RunpamineTheme {
        TeamScreen(
            onInviteClick = {},
        )
    }
}
