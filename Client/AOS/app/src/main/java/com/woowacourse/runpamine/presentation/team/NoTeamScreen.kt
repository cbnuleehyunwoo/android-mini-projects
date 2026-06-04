package com.woowacourse.runpamine.presentation.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.presentation.team.components.NoTeamButtonSection
import com.woowacourse.runpamine.presentation.team.components.NoTeamHeader
import com.woowacourse.runpamine.presentation.team.components.NoTeamIcon
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun NoTeamScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NoTeamIcon()
        Spacer(modifier = Modifier.height(10.dp))
        NoTeamHeader()
        Spacer(modifier = Modifier.height(10.dp))
        NoTeamButtonSection()
    }
}

@Preview(showBackground = true)
@Composable
private fun NoTeamScreenPreview() {
    RunpamineTheme {
        NoTeamScreen()
    }
}
