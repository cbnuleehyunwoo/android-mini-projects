package com.woowacourse.runpamine.presentation.team.components.team

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.presentation.team.model.TeamMember
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun TeamMemberCard(
    member: TeamMember,
    calories: String,
    distance: String,
    time: String,
    pace: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(18.dp)

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .height(168.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = cardShape,
                    ambientColor = Color.Transparent,
                    spotColor = Color.Black.copy(alpha = 0.8f),
                ).background(
                    color = Color.White,
                    shape = cardShape,
                ).clickable(onClick = onClick)
                .padding(25.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = member.name,
                modifier = Modifier.weight(1f, fill = false),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (member.isMe) {
                Text(
                    text = "나",
                    modifier =
                        Modifier
                            .background(
                                color = Color(0xFF0D5BFF),
                                shape = RoundedCornerShape(8.dp),
                            ).padding(horizontal = 6.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            RunnerThumbnail(
                runningStatus = member.runningStatus,
                modifier = Modifier.size(80.dp),
            )
            RunningMetricSection(
                distance = distance,
                time = time,
                pace = pace,
                modifier = Modifier.weight(1f),
            )
            CalorieMetricCard(
                calories = calories,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 500)
@Composable
private fun TeamMemberPreview() {
    RunpamineTheme {
        TeamMemberCard(
            member =
                TeamMember(
                    id = "1",
                    name = "커비커비커비커비커비",
                    distance = "12.3 km",
                    time = "28:35",
                    pace = "2'19\"",
                    calories = "344",
                    isMe = true,
                ),
            distance = "12.3km",
            time = "22:32",
            pace = "5'30\"",
            calories = "344",
            onClick = {},
        )
    }
}
