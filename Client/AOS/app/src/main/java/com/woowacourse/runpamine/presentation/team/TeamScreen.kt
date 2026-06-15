package com.woowacourse.runpamine.presentation.team

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
    onMemberClick: (TeamMember) -> Unit,
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
        onPreviousDateClick = viewModel::moveToPreviousDate,
        onNextDateClick = viewModel::moveToNextDate,
        onMemberClick = onMemberClick,
        modifier = modifier,
    )
}

@Composable
private fun TeamScreenContent(
    uiState: TeamUiState,
    onInviteClick: (String) -> Unit,
    onJoinTeamClick: () -> Unit,
    onCreateTeamClick: () -> Unit,
    onPreviousDateClick: () -> Unit,
    onNextDateClick: () -> Unit,
    onMemberClick: (TeamMember) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> {
            TeamSkeletonContent(modifier = modifier.fillMaxSize())
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
                onPreviousDateClick = onPreviousDateClick,
                onNextDateClick = onNextDateClick,
                canMoveToNextDate = uiState.canMoveToNextDate,
                isDateLoading = uiState.isDateLoading,
                onMemberClick = onMemberClick,
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

@Composable
private fun TeamSkeletonContent(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            TeamHeaderSkeleton()
        }
        item {
            TeamDateSkeleton()
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TeamSummarySkeletonCard(modifier = Modifier.weight(1f))
                TeamSummarySkeletonCard(modifier = Modifier.weight(1f))
            }
        }
        items(TEAM_MEMBER_SKELETON_COUNT) {
            TeamMemberSkeletonCard()
        }
    }
}

@Composable
private fun TeamHeaderSkeleton(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TeamSkeletonBox(
            modifier =
                Modifier
                    .height(36.dp)
                    .weight(1f),
            shape = RoundedCornerShape(12.dp),
        )
        Spacer(modifier = Modifier.width(48.dp))
        TeamSkeletonBox(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(14.dp),
        )
    }
}

@Composable
private fun TeamDateSkeleton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        TeamSkeletonBox(
            modifier =
                Modifier
                    .fillMaxWidth(0.58f)
                    .height(24.dp),
            shape = RoundedCornerShape(12.dp),
        )
    }
}

@Composable
private fun TeamSummarySkeletonCard(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .height(72.dp)
                .teamSkeletonCard(shape = RoundedCornerShape(14.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        TeamSkeletonBox(
            modifier =
                Modifier
                    .fillMaxWidth(0.52f)
                    .height(24.dp),
            shape = RoundedCornerShape(10.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        TeamSkeletonBox(
            modifier =
                Modifier
                    .fillMaxWidth(0.48f)
                    .height(14.dp),
            shape = RoundedCornerShape(8.dp),
        )
    }
}

@Composable
private fun TeamMemberSkeletonCard(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .height(168.dp)
                .teamSkeletonCard(shape = RoundedCornerShape(18.dp))
                .padding(25.dp),
    ) {
        TeamSkeletonBox(
            modifier =
                Modifier
                    .fillMaxWidth(0.42f)
                    .height(28.dp),
            shape = RoundedCornerShape(10.dp),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp),
        ) {
            TeamSkeletonBox(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(0.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                repeat(TEAM_MEMBER_METRIC_SKELETON_COUNT) {
                    TeamMetricSkeletonRow()
                }
            }
            TeamSkeletonBox(
                modifier =
                    Modifier
                        .width(58.dp)
                        .height(76.dp),
                shape = RoundedCornerShape(12.dp),
            )
        }
    }
}

@Composable
private fun TeamMetricSkeletonRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TeamSkeletonBox(
            modifier = Modifier.size(14.dp),
            shape = RoundedCornerShape(5.dp),
        )
        TeamSkeletonBox(
            modifier =
                Modifier
                    .width(28.dp)
                    .height(13.dp),
            shape = RoundedCornerShape(6.dp),
        )
        TeamSkeletonBox(
            modifier =
                Modifier
                    .width(54.dp)
                    .height(13.dp),
            shape = RoundedCornerShape(6.dp),
        )
    }
}

@Composable
private fun TeamSkeletonBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(8.dp),
) {
    Box(
        modifier =
            modifier
                .clip(shape)
                .background(TeamSkeletonColor),
    )
}

private fun Modifier.teamSkeletonCard(shape: RoundedCornerShape): Modifier =
    shadow(
        elevation = 8.dp,
        shape = shape,
        ambientColor = Color.Transparent,
        spotColor = Color.Black.copy(alpha = 0.18f),
    ).background(
        color = Color.White,
        shape = shape,
    )

private const val TEAM_MEMBER_SKELETON_COUNT = 4
private const val TEAM_MEMBER_METRIC_SKELETON_COUNT = 3
private val TeamSkeletonColor = Color(0xFFE8EEF6)

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
            onPreviousDateClick = {},
            onNextDateClick = {},
            onMemberClick = {},
        )
    }
}
