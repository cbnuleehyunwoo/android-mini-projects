package com.woowacourse.runpamine.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.woowacourse.runpamine.di.runpamineContainer
import com.woowacourse.runpamine.presentation.home.components.HomeHeader
import com.woowacourse.runpamine.presentation.home.components.HomeMapSection
import com.woowacourse.runpamine.presentation.home.components.HomeNoTeamSection
import com.woowacourse.runpamine.presentation.home.components.HomeTeamSection
import com.woowacourse.runpamine.presentation.home.components.StartButton
import com.woowacourse.runpamine.presentation.home.viewmodel.HomeUiState
import com.woowacourse.runpamine.presentation.home.viewmodel.HomeViewModel
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun HomeScreen(
    onCreateTeamClick: () -> Unit,
    onMyPageClick: () -> Unit,
    onJoinTeamClick: () -> Unit,
    onStartClick: () -> Unit,
    onTeamClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.runpamineContainer
    val viewModel: HomeViewModel =
        viewModel(
            factory = HomeViewModel.Factory(container.profileRepository),
        )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        uiState = uiState,
        onCreateTeamClick = onCreateTeamClick,
        onMyPageClick = onMyPageClick,
        onJoinTeamClick = onJoinTeamClick,
        onStartClick = onStartClick,
        onTeamClick = onTeamClick,
        modifier = modifier,
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onCreateTeamClick: () -> Unit,
    onMyPageClick: () -> Unit,
    onJoinTeamClick: () -> Unit,
    onStartClick: () -> Unit,
    onTeamClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        HomeHeader(
            name = uiState.nickname.ifBlank { "러너" },
            onMyPageClick = onMyPageClick,
        )
        uiState.teamName?.let { teamName ->
            HomeTeamSection(
                teamName = teamName,
                onClick = onTeamClick,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
        } ?: HomeNoTeamSection(
            onCreate = onCreateTeamClick,
            onJoin = onJoinTeamClick,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier.weight(1f),
        ) {
            HomeMapSection(modifier = Modifier.fillMaxSize())
            StartButton(
                onClick = onStartClick,
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    RunpamineTheme {
        HomeContent(
            uiState = HomeUiState(nickname = "러너"),
            onCreateTeamClick = {},
            onJoinTeamClick = {},
            onStartClick = {},
            onMyPageClick = {},
            onTeamClick = {},
        )
    }
}
