package com.woowacourse.runpamine.presentation.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.di.runpamineContainer
import com.woowacourse.runpamine.presentation.team.components.team.TeamContent
import com.woowacourse.runpamine.presentation.team.model.TeamMember
import com.woowacourse.runpamine.presentation.team.viewmodel.TeamUiState
import com.woowacourse.runpamine.presentation.team.viewmodel.TeamViewModel
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun TeamScreen(
    onInviteClick: (String) -> Unit,
    onJoinTeamClick: () -> Unit,
    onCreateTeamClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.runpamineContainer
    val viewModel: TeamViewModel =
        viewModel(
            factory = TeamViewModel.Factory(container.teamRepository),
        )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TeamScreenContent(
        uiState = uiState,
        onInviteClick = onInviteClick,
        onJoinTeamClick = onJoinTeamClick,
        onCreateTeamClick = onCreateTeamClick,
        modifier = modifier,
    )
}

@Composable
private fun TeamScreenContent(
    uiState: TeamUiState,
    onInviteClick: (String) -> Unit,
    onJoinTeamClick: () -> Unit,
    onCreateTeamClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> {
            Column(
                modifier = modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.team_loading),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        uiState.hasTeam -> {
            TeamContent(
                teamName = uiState.teamName,
                date = uiState.date,
                totalDistance = uiState.totalDistance,
                completedMemberCount = uiState.completedMemberCount,
                totalMemberCount = uiState.totalMemberCount,
                members = uiState.members,
                onAddClick = { onInviteClick(uiState.joinCode) },
                modifier = modifier.fillMaxSize(),
                memberErrorMessage = uiState.memberErrorMessage,
            )
        }

        else -> {
            NoTeamScreen(
                onJoinTeamClick = onJoinTeamClick,
                onCreateTeamClick = onCreateTeamClick,
                modifier = modifier.fillMaxSize(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TeamScreenPreview() {
    RunpamineTheme {
        TeamScreenContent(
            uiState =
                TeamUiState(
                    hasTeam = true,
                    teamName = "볼트 멋쟁이",
                    joinCode = "ADOM34",
                    date = "2026년 6월 2일 - 화요일",
                    totalDistance = "1.1 km",
                    completedMemberCount = 1,
                    totalMemberCount = 1,
                    members =
                        listOf(
                            TeamMember(
                                id = "1",
                                name = "러너",
                                distance = "1.1 km",
                                time = "33:41",
                                pace = "30'37\"",
                                calories = "200",
                            ),
                        ),
                    isLoading = false,
                ),
            onInviteClick = {},
            onJoinTeamClick = {},
            onCreateTeamClick = {},
        )
    }
}
