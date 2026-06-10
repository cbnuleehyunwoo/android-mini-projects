package com.woowacourse.runpamine.presentation.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.woowacourse.runpamine.R
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
    var showStartDialog by rememberSaveable { mutableStateOf(false) }

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
                onClick = { showStartDialog = true },
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
            )
        }
    }

    if (showStartDialog) {
        RunningStartDialog(
            onDismiss = { showStartDialog = false },
            onConfirm = {
                showStartDialog = false
                onStartClick()
            },
        )
    }
}

@Composable
private fun RunningStartDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        val dialogWindow = (LocalView.current.parent as DialogWindowProvider).window
        SideEffect {
            dialogWindow.setDimAmount(0.01f)
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.running_start_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.running_start_confirmation),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        colors =
                            ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary,
                            ),
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                    ) {
                        Text(
                            text = stringResource(R.string.start),
                            color = Color.White,
                        )
                    }
                }
            }
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
