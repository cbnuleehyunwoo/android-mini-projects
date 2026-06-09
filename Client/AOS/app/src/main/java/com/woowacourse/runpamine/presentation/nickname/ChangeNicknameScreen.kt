package com.woowacourse.runpamine.presentation.nickname

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
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
import com.woowacourse.runpamine.presentation.nickname.viewmodel.NICKNAME_MAX_LENGTH
import com.woowacourse.runpamine.presentation.nickname.viewmodel.NICKNAME_MIN_LENGTH
import com.woowacourse.runpamine.presentation.nickname.viewmodel.NICKNAME_REGEX
import com.woowacourse.runpamine.presentation.nickname.viewmodel.NicknameUiState
import com.woowacourse.runpamine.presentation.nickname.viewmodel.NicknameViewModel
import com.woowacourse.runpamine.ui.theme.Blue40
import com.woowacourse.runpamine.ui.theme.Green40
import com.woowacourse.runpamine.ui.theme.Red40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun ChangeNicknameScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onCompleted: () -> Unit = {},
) {
    val container = androidx.compose.ui.platform.LocalContext.current.runpamineContainer
    val viewModel: NicknameViewModel =
        viewModel(
            factory = NicknameViewModel.Factory(container.profileRepository),
        )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) {
            viewModel.onCompletedHandled()
            onCompleted()
        }
    }

    ChangeNicknameContent(
        uiState = uiState,
        onNicknameChange = viewModel::updateNickname,
        onSubmitClick = viewModel::createProfile,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@Composable
private fun ChangeNicknameContent(
    uiState: NicknameUiState,
    onNicknameChange: (String) -> Unit,
    onSubmitClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
    ) {
        ScreenTopBar(
            title = stringResource(R.string.change_nickname_bar),
            onBackClick = onBackClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(64.dp),
        )
        Spacer(modifier = Modifier.height(15.dp))
        Text(
            text = stringResource(R.string.change_nickname_description),
            style =
                MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 36.sp,
                    lineHeight = 50.sp,
                ),
            fontWeight = FontWeight.Black,
            color = Color.Black,
        )
        Spacer(modifier = Modifier.height(28.dp))
        NicknameTextField(
            value = uiState.nickname,
            onValueChange = onNicknameChange,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))
        NicknameCondition(
            text = stringResource(R.string.change_nickname_condition_length),
            valid = uiState.nickname.length in NICKNAME_MIN_LENGTH..NICKNAME_MAX_LENGTH,
            neutral = uiState.nickname.isEmpty(),
        )
        Spacer(modifier = Modifier.height(14.dp))
        NicknameCondition(
            text = stringResource(R.string.change_nickname_condition_characters),
            valid = uiState.nickname.isEmpty() || NICKNAME_REGEX.matches(uiState.nickname),
        )
        Spacer(modifier = Modifier.height(14.dp))
        NicknameCondition(
            text = stringResource(R.string.change_nickname_condition_special),
            valid = false,
            neutral = uiState.nickname.isEmpty() || NICKNAME_REGEX.matches(uiState.nickname),
            positiveWhenNeutral = false,
        )
        uiState.errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Red40,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        BottomButton(
            text =
                if (uiState.isLoading) {
                    "설정 중..."
                } else {
                    stringResource(R.string.change_nickname_button)
                },
            onClick = onSubmitClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
        )
    }
}

@Composable
private fun NicknameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.height(58.dp),
        placeholder = {
            Text(
                text = stringResource(R.string.change_nickname_placeholder),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 22.sp),
                color = Color(0xFF6B7280),
            )
        },
        leadingIcon = {
            Image(
                painter = painterResource(id = R.drawable.ic_profile),
                contentDescription = "닉네임",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(Color(0xFF9CA3AF)),
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Blue40,
                unfocusedBorderColor = Blue40,
                cursorColor = Blue40,
            ),
    )
}

@Composable
private fun NicknameCondition(
    text: String,
    valid: Boolean,
    modifier: Modifier = Modifier,
    neutral: Boolean = false,
    positiveWhenNeutral: Boolean = true,
) {
    val conditionColor =
        when {
            neutral && positiveWhenNeutral -> Green40
            neutral -> Red40
            valid -> Green40
            else -> Red40
        }
    val mark = if (conditionColor == Green40) "✓" else "×"

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = mark,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 24.sp, lineHeight = 24.sp),
            color = conditionColor,
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 21.sp, lineHeight = 26.sp),
            color = conditionColor,
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun ChangeNicknameScreenPreview() {
    RunpamineTheme {
        ChangeNicknameContent(
            uiState = NicknameUiState(),
            onNicknameChange = {},
            onSubmitClick = {},
            onBackClick = {},
        )
    }
}
