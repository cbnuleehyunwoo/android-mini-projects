package com.woowacourse.runpamine.presentation.team.components.noteam

import android.R.attr.contentDescription
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun NoTeamButtonSection(
    onCreate: () -> Unit,
    onJoin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CreateTeamButton(
            onClick = onCreate,
        )
        JoinTeamButton(
            onClick = onJoin,
        )
    }
}

@Composable
private fun CreateTeamButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(24.dp),
                ).clickable { onClick() }
                .fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier =
                Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_plus),
                contentDescription = "팀 생성하기",
                tint = Color.White,
                modifier = Modifier.size(32.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.create_team),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                )
                Text(
                    text = stringResource(R.string.no_team_create_team_button),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                )
            }
            Icon(
                painter = painterResource(R.drawable.right_arrow),
                contentDescription = "이동하기",
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun JoinTeamButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp))
                .clickable(onClick = onClick)
                .fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            modifier =
                Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
        ) {
            Icon(
                painterResource(R.drawable.key_bold),
                contentDescription = "팀 생성하기",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.join_team),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.no_team_join_team_button),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Icon(
                painter = painterResource(R.drawable.right_arrow),
                contentDescription = "이동하기",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun NoTeamButtonSectionPreview() {
    RunpamineTheme {
        NoTeamButtonSection(
            modifier = Modifier.fillMaxWidth(),
            onJoin = {},
            onCreate = {},
        )
    }
}
