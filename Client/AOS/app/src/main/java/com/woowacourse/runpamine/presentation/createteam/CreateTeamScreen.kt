package com.woowacourse.runpamine.presentation.createteam

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.woowacourse.runpamine.di.runpamineContainer
import com.woowacourse.runpamine.presentation.createteam.components.CreateTeamContent
import com.woowacourse.runpamine.presentation.createteam.viewmodel.CreateTeamViewModel
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun CreateTeamScreen(
    onCreateSuccess: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.runpamineContainer
    val viewModel: CreateTeamViewModel =
        viewModel(
            factory = CreateTeamViewModel.Factory(container.teamRepository),
        )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.createdJoinCode) {
        uiState.createdJoinCode?.let(onCreateSuccess)
    }

    CreateTeamContent(
        teamName = uiState.teamName,
        isLengthValid = uiState.isLengthValid,
        hasAllowedCharacters = uiState.hasAllowedCharacters,
        hasNoSpecialCharacters = uiState.hasNoSpecialCharacters,
        onTeamNameChange = viewModel::updateTeamName,
        validator = viewModel::isValidTeamName,
        onCreateClick = viewModel::createTeam,
        onBackClick = onBackClick,
        modifier = modifier,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
    )
}

@Preview(showBackground = true)
@Composable
private fun CreateTeamScreenPreview() {
    RunpamineTheme {
        CreateTeamContent(
            teamName = "러닝크루",
            isLengthValid = true,
            hasAllowedCharacters = true,
            hasNoSpecialCharacters = true,
            onTeamNameChange = {},
            validator = { true },
            onBackClick = {},
            onCreateClick = {},
        )
    }
}

@Preview(showBackground = true, name = "검증 실패")
@Composable
private fun CreateTeamScreenErrorPreview() {
    RunpamineTheme {
        CreateTeamContent(
            teamName = "팀!",
            isLengthValid = false,
            hasAllowedCharacters = false,
            hasNoSpecialCharacters = false,
            onTeamNameChange = {},
            validator = { false },
            onCreateClick = {},
            onBackClick = {},
        )
    }
}
