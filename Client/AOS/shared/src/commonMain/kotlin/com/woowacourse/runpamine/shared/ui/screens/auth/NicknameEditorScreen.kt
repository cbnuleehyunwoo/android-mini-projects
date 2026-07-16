package com.woowacourse.runpamine.shared.ui.screens.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.shared.ui.components.PrimaryButton
import com.woowacourse.runpamine.shared.ui.components.TopNavigationBar
import com.woowacourse.runpamine.shared.ui.components.ValidationRuleRow
import com.woowacourse.runpamine.shared.ui.theme.RunpamineColors
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTheme
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTypography

enum class NicknameEditorMode {
    Setup,
    Change,
}

@Composable
fun NicknameEditorScreen(
    nickname: String,
    mode: NicknameEditorMode,
    isLoading: Boolean,
    errorMessage: String?,
    onNicknameChange: (String) -> Unit,
    onBack: () -> Unit,
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val trimmedNickname = nickname.trim()
    val hasValidLength = trimmedNickname.length in 2..10
    val containsOnlyAllowedCharacters = trimmedNickname.isNotEmpty() && AllowedNickname.matches(trimmedNickname)
    val doesNotContainSpecialCharacters = trimmedNickname.isEmpty() || !SpecialCharacter.containsMatchIn(trimmedNickname)
    val canSubmit = hasValidLength && containsOnlyAllowedCharacters && doesNotContainSpecialCharacters

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White)
                .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        TopNavigationBar(
            title = "닉네임 변경",
            onBack = onBack,
            closeStyle = mode == NicknameEditorMode.Change,
            modifier = Modifier.padding(horizontal = 18.dp).padding(top = 8.dp),
        )

        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 32.dp),
        ) {
            Text(
                text = "사용할 닉네임을\n입력해주세요",
                style = mode.headingStyle,
                color = Color.Black,
                modifier = Modifier.padding(top = mode.headingTopPadding),
            )

            NicknameField(
                nickname = nickname,
                placeholder = mode.placeholder,
                alwaysFocusedBorder = mode == NicknameEditorMode.Change,
                onNicknameChange = onNicknameChange,
                modifier = Modifier.padding(top = 18.dp),
            )

            Column(
                modifier = Modifier.padding(top = 18.dp, start = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ValidationRuleRow(text = "2-10자 이내", isValid = hasValidLength)
                ValidationRuleRow(text = "한글, 영문, 숫자 사용 가능", isValid = containsOnlyAllowedCharacters)
                ValidationRuleRow(text = "특수문자 사용 불가", isValid = doesNotContainSpecialCharacters)
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = if (mode == NicknameEditorMode.Setup) RunpamineTypography.Body2 else RunpamineTypography.Caption1,
                    color = RunpamineColors.Danger,
                    modifier = Modifier.padding(top = 14.dp, start = 8.dp),
                )
            }

            Spacer(Modifier.weight(1f))

            PrimaryButton(
                title = mode.buttonTitle,
                onClick = { onSubmit(trimmedNickname) },
                enabled = canSubmit,
                isLoading = isLoading,
                modifier = Modifier.padding(bottom = 34.dp),
            )
        }
    }
}

@Composable
private fun NicknameField(
    nickname: String,
    placeholder: String,
    alwaysFocusedBorder: Boolean,
    onNicknameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val hasPrimaryBorder = alwaysFocusedBorder || isFocused
    val borderColor = if (hasPrimaryBorder) RunpamineColors.Primary else RunpamineColors.Border
    val borderWidth = if (hasPrimaryBorder) 2.dp else 1.dp

    LaunchedEffect(focusRequester) {
        focusRequester.requestFocus()
    }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(borderWidth, borderColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PersonOutlineIcon()

        BasicTextField(
            value = nickname,
            onValueChange = onNicknameChange,
            modifier =
                Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged { isFocused = it.isFocused },
            textStyle = RunpamineTypography.Title2.copy(color = Color.Black),
            singleLine = true,
            cursorBrush = SolidColor(RunpamineColors.Primary),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (nickname.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = RunpamineTypography.Title2,
                            color = RunpamineColors.TextSecondary.copy(alpha = 0.55f),
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
private fun PersonOutlineIcon(modifier: Modifier = Modifier) {
    val iconColor = RunpamineColors.TextSecondary.copy(alpha = 0.7f)
    Canvas(modifier = modifier.size(22.dp)) {
        val strokeWidth = 1.8.dp.toPx()
        drawCircle(
            color = iconColor,
            radius = size.minDimension * 0.18f,
            center = Offset(size.width / 2f, size.height * 0.29f),
            style = Stroke(width = strokeWidth),
        )
        drawArc(
            color = iconColor,
            startAngle = 195f,
            sweepAngle = 150f,
            useCenter = false,
            topLeft = Offset(size.width * 0.18f, size.height * 0.48f),
            size = Size(size.width * 0.64f, size.height * 0.54f),
            style = Stroke(width = strokeWidth),
        )
    }
}

private val NicknameEditorMode.headingTopPadding
    get() = if (this == NicknameEditorMode.Setup) 22.dp else 28.dp

private val NicknameEditorMode.placeholder
    get() = if (this == NicknameEditorMode.Setup) "예: 커비" else "예: 호이"

private val NicknameEditorMode.buttonTitle
    get() = if (this == NicknameEditorMode.Setup) "런파민 시작하기" else "변경하기"

private val NicknameEditorMode.headingStyle: TextStyle
    get() =
        if (this == NicknameEditorMode.Setup) {
            RunpamineTypography.Header1.copy(lineHeight = 41.sp)
        } else {
            RunpamineTypography.Header1.copy(fontSize = 29.sp, fontWeight = FontWeight.Black, lineHeight = 42.sp)
        }

private val AllowedNickname = Regex("^[가-힣ㄱ-ㅎㅏ-ㅣA-Za-z0-9]+$")
private val SpecialCharacter = Regex("[^가-힣ㄱ-ㅎㅏ-ㅣA-Za-z0-9]")

@Preview
@Composable
private fun NicknameSetupScreenPreview() {
    RunpamineTheme {
        NicknameEditorScreen(
            nickname = "커비",
            mode = NicknameEditorMode.Setup,
            isLoading = false,
            errorMessage = null,
            onNicknameChange = {},
            onBack = {},
            onSubmit = {},
        )
    }
}

@Preview
@Composable
private fun NicknameChangeScreenPreview() {
    RunpamineTheme {
        NicknameEditorScreen(
            nickname = "호이",
            mode = NicknameEditorMode.Change,
            isLoading = false,
            errorMessage = null,
            onNicknameChange = {},
            onBack = {},
            onSubmit = {},
        )
    }
}
