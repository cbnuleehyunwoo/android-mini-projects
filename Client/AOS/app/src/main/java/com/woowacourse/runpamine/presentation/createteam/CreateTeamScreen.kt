package com.woowacourse.runpamine.presentation.createteam

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.woowacourse.runpamine.presentation.createteam.components.CreateTeamContent
import com.woowacourse.runpamine.presentation.createteam.viewmodel.CreateTeamViewModel
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun CreateTeamScreen(
    modifier: Modifier = Modifier,
    viewModel: CreateTeamViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CreateTeamContent(
        teamName = uiState.teamName,
        isLengthValid = uiState.isLengthValid,
        hasAllowedCharacters = uiState.hasAllowedCharacters,
        hasNoSpecialCharacters = uiState.hasNoSpecialCharacters,
        onTeamNameChange = viewModel::updateTeamName,
        validator = viewModel::isValidTeamName,
        onCreateClick = {},
        modifier = modifier,
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
        )
    }
}
