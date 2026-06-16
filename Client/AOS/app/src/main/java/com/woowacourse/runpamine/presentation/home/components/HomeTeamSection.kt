package com.woowacourse.runpamine.presentation.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.Blue40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun HomeTeamSection(
    teamName: String,
    todayRunMemberCount: Int,
    teamMemberCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ).clip(RoundedCornerShape(26.dp))
                .background(Blue40)
                .padding(horizontal = 22.dp, vertical = 22.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(end = 72.dp),
        ) {
            Text(
                text = teamName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 18.sp,
                    ),
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text =
                    stringResource(
                        R.string.home_team_today_runner_count,
                        todayRunMemberCount,
                        teamMemberCount,
                    ),
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                    ),
                color = Color.White.copy(alpha = 0.78f),
                fontWeight = FontWeight.Medium,
            )
        }
        Image(
            painter = painterResource(R.drawable.include_users),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.72f)),
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(54.dp)
                    .alpha(0.9f),
        )
    }
}

@Preview(showBackground = true, widthDp = 393)
@Composable
private fun HomeTeamSectionPreview() {
    RunpamineTheme {
        HomeTeamSection(
            teamName = "우테코8기AN",
            todayRunMemberCount = 8,
            teamMemberCount = 10,
            onClick = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}
