package com.woowacourse.runpamine.presentation.team.components.team

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.presentation.team.model.TeamMember
import com.woowacourse.runpamine.ui.theme.Blue40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun TeamMemberCard(
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

@Preview(showBackground = true)
@Composable
private fun TeamMemberPreview() {
    RunpamineTheme {
        TeamMemberCard(
            member =
                TeamMember(
                    id = 1L,
                    name = "커비커비커비커비커비",
                    distance = "12.3",
                    time = "28:35",
                ),
        )
    }
}
