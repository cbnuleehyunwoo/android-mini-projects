package com.woowacourse.runpamine.presentation.join

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.di.runpamineContainer
import com.woowacourse.runpamine.presentation.component.BottomButton
import com.woowacourse.runpamine.presentation.component.ScreenTopBar
import com.woowacourse.runpamine.presentation.component.ValidatableTextField
import com.woowacourse.runpamine.ui.theme.Red40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun JoinScreen(
    onBackClick: () -> Unit,
    onJoinSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val container = LocalContext.current.runpamineContainer
    val viewModel: JoinViewModel =
        viewModel(
            factory = JoinViewModel.Factory(container.teamRepository),
        )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isJoined) {
        if (uiState.isJoined) onJoinSuccess()
    }

    JoinContent(
        uiState = uiState,
        onCodeChange = viewModel::updateCode,
        onJoinClick = viewModel::joinTeam,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
private fun JoinContent(
    uiState: JoinUiState,
    onCodeChange: (String) -> Unit,
    onJoinClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .padding(horizontal = 24.dp, vertical = 14.dp),
    ) {
        ScreenTopBar(
            title = stringResource(R.string.join_team_bar),
            onBackClick = onBackClick,
        )
        Spacer(
            modifier = Modifier.height(15.dp),
        )
        Text(
            text = stringResource(R.string.join_team_description),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            lineHeight = 40.sp,
        )
        Spacer(
            modifier = Modifier.height(15.dp),
        )
        ValidatableTextField(
            value = uiState.code,
            onValueChange = onCodeChange,
            placeholder = stringResource(R.string.join_code_placeholder),
            keyboardType = KeyboardType.Ascii,
            modifier = Modifier.fillMaxWidth(),
        )
        uiState.errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                color = Red40,
                fontSize = 13.sp,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        BottomButton(
            text = stringResource(R.string.join_team),
            onClick = onJoinClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && uiState.code.length == JOIN_CODE_MAX_LENGTH,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun JoinScreenPreview() {
    RunpamineTheme {
        JoinContent(
            uiState = JoinUiState(),
            onCodeChange = {},
            onJoinClick = {},
            onBackClick = {},
        )
    }
}
