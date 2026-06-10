package com.woowacourse.runpamine.presentation.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.presentation.team.components.noteam.NoTeamButtonSection
import com.woowacourse.runpamine.presentation.team.components.noteam.NoTeamHeader
import com.woowacourse.runpamine.presentation.team.components.noteam.NoTeamIcon
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun NoTeamScreen(
    onJoinTeamClick: () -> Unit,
    onCreateTeamClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 22.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NoTeamIcon()
        Spacer(modifier = Modifier.height(10.dp))
        NoTeamHeader()
        Spacer(modifier = Modifier.height(10.dp))
        NoTeamButtonSection(
            onCreate = onCreateTeamClick,
            onJoin = onJoinTeamClick,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoTeamScreenPreview() {
    RunpamineTheme {
        NoTeamScreen(
            onJoinTeamClick = {},
            onCreateTeamClick = {},
        )
    }
}
