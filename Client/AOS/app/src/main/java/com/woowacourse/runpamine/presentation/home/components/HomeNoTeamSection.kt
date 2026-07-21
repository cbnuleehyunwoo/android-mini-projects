package com.woowacourse.runpamine.presentation.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.Blue40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun HomeNoTeamSection(
    onCreate: () -> Unit,
    onJoin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Blue40)
                .padding(horizontal = 20.dp, vertical = 18.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(end = 40.dp),
        ) {
            Text(
                text = stringResource(R.string.home_no_team_title),
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 17.sp,
                    ),
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.home_no_team_description),
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 13.sp,
                    ),
                color = Color.White.copy(alpha = 0.75f),
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(7.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NoTeamButton(
                    text = stringResource(R.string.create_team),
                    filled = true,
                    onClick = onCreate,
                )
                NoTeamButton(
                    text = stringResource(R.string.join_team),
                    filled = false,
                    onClick = onJoin,
                )
            }
        }
        Image(
            painter = painterResource(R.drawable.include_users),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Color.White.copy(alpha = 0.72f)),
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(32.dp)
                    .alpha(0.9f),
        )
    }
}

@Composable
private fun NoTeamButton(
    text: String,
    filled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(28.dp)
    val backgroundColor = if (filled) Color.White else Color.Transparent
    val contentColor = if (filled) Blue40 else Color.White

    Box(
        modifier =
            modifier
                .width(86.dp)
                .height(30.dp)
                .clip(shape)
                .background(backgroundColor)
                .border(
                    width = 1.5.dp,
                    color = Color.White,
                    shape = shape,
                ).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(name = "Light Mode", showBackground = true, widthDp = 393)
@Composable
private fun HomeNoTeamSectionPreview() {
    RunpamineTheme {
        HomeNoTeamSection(
            onJoin = {},
            onCreate = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}
