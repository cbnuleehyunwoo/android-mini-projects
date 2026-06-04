package com.woowacourse.runpamine.presentation.nickname

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.presentation.component.BottomButton
import com.woowacourse.runpamine.presentation.component.ScreenTopBar
import com.woowacourse.runpamine.ui.theme.Blue40
import com.woowacourse.runpamine.ui.theme.Green40
import com.woowacourse.runpamine.ui.theme.Red40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

private const val NICKNAME_MIN_LENGTH = 2
private const val NICKNAME_MAX_LENGTH = 10
private val NICKNAME_REGEX = Regex("^[가-힣a-zA-Z0-9]*$")

@Composable
fun ChangeNicknameScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
) {
    var nickname by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .safeDrawingPadding()
                .padding(horizontal = 24.dp),
        ) {
            ScreenTopBar(
                title = stringResource(R.string.change_nickname_bar),
                onBackClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
            )
            Spacer(modifier = Modifier.height(15.dp))
            Text(
                text = stringResource(R.string.change_nickname_description),
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp, lineHeight = 50.sp),
                fontWeight = FontWeight.Black,
                color = Color.Black,
            )
            Spacer(modifier = Modifier.height(28.dp))
            NicknameTextField(
                value = nickname,
                onValueChange = { input ->
                    if (input.length <= NICKNAME_MAX_LENGTH && NICKNAME_REGEX.matches(input)) {
                        nickname = input
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(24.dp))
            NicknameCondition(
                text = stringResource(R.string.change_nickname_condition_length),
                valid = nickname.length in NICKNAME_MIN_LENGTH..NICKNAME_MAX_LENGTH,
                neutral = nickname.isEmpty(),
            )
            Spacer(modifier = Modifier.height(14.dp))
            NicknameCondition(
                text = stringResource(R.string.change_nickname_condition_characters),
                valid = nickname.isEmpty() || NICKNAME_REGEX.matches(nickname),
            )
            Spacer(modifier = Modifier.height(14.dp))
            NicknameCondition(
                text = stringResource(R.string.change_nickname_condition_special),
                valid = false,
                neutral = nickname.isEmpty() || NICKNAME_REGEX.matches(nickname),
                positiveWhenNeutral = false,
            )
            Spacer(modifier = Modifier.weight(1f))
            BottomButton(
                text = stringResource(R.string.change_nickname_button),
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            )
        }
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
        colors = OutlinedTextFieldDefaults.colors(
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
    val conditionColor = when {
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
        ChangeNicknameScreen()
    }
}
