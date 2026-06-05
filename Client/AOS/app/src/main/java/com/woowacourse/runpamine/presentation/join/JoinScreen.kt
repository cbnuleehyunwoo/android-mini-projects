package com.woowacourse.runpamine.presentation.join

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.presentation.component.BottomButton
import com.woowacourse.runpamine.presentation.component.ScreenTopBar
import com.woowacourse.runpamine.presentation.component.ValidatableTextField
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

// 팀 참가 코드: 영문, 숫자로 최대 6글자
private const val JOIN_CODE_MAX_LENGTH = 6
private val JOIN_CODE_REGEX = Regex("^[a-zA-Z0-9]*$")

@Composable
fun JoinScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var code by remember { mutableStateOf("") }

    Column(
        modifier = modifier,
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
        ValidatableTextField(
            value = code,
            onValueChange = { input ->
                if (input.length <= JOIN_CODE_MAX_LENGTH && JOIN_CODE_REGEX.matches(input)) {
                    code = input
                }
            },
            placeholder = stringResource(R.string.join_code_placeholder),
            keyboardType = KeyboardType.Ascii,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.weight(1f))
        BottomButton(
            text = stringResource(R.string.join_team),
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun JoinScreenPreview() {
    RunpamineTheme {
        JoinScreen(
            onBackClick = {},
        )
    }
}
