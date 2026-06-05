package com.woowacourse.runpamine.presentation.team

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.woowacourse.runpamine.presentation.team.components.team.TeamContent
import com.woowacourse.runpamine.presentation.team.model.TeamMember
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun TeamScreen(
    onInviteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val members =
        remember {
            listOf(
                TeamMember(id = 1L, name = "커비커비커비커비커", distance = "12.3", time = "28:35"),
                TeamMember(id = 2L, name = "호이", distance = "1.1", time = "33:41"),
                TeamMember(id = 3L, name = "볼트트", distance = "9.2", time = "30:30"),
                TeamMember(id = 4L, name = "커비커비커비커비커", distance = "10.2", time = "31:58"),
            )
        }

    TeamContent(
        teamName = "볼트 멋쟁이",
        date = "2026년 6월 2일 - 화요일",
        totalDistance = "324 km",
        completedMemberCount = 3,
        totalMemberCount = 4,
        members = members,
        onAddClick = onInviteClick,
        modifier = modifier.fillMaxSize(),
    )
}

@Preview(showBackground = true)
@Composable
private fun TeamScreenPreview() {
    RunpamineTheme {
        TeamScreen(
            onInviteClick = {},
        )
    }
}
