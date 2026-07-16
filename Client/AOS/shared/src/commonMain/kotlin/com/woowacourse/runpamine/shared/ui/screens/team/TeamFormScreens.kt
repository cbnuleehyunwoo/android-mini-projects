package com.woowacourse.runpamine.shared.ui.screens.team

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.shared.generated.resources.Res
import com.woowacourse.runpamine.shared.generated.resources.icon_team
import com.woowacourse.runpamine.shared.ui.components.PrimaryButton
import com.woowacourse.runpamine.shared.ui.components.TopNavigationBar
import com.woowacourse.runpamine.shared.ui.components.ValidationRuleRow
import com.woowacourse.runpamine.shared.ui.theme.RunpamineColors
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTheme
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTypography
import org.jetbrains.compose.resources.painterResource

data class TeamCreateUiState(
    val teamName: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val hasValidLength: Boolean
        get() = teamName.length in TEAM_NAME_MIN_LENGTH..TEAM_NAME_MAX_LENGTH

    val containsOnlyAllowedCharacters: Boolean
        get() = teamName.isNotEmpty() && TEAM_NAME_ALLOWED_REGEX.matches(teamName)

    val doesNotContainSpecialCharacters: Boolean
        get() = teamName.isNotEmpty() && !TEAM_NAME_SPECIAL_CHARACTER_REGEX.containsMatchIn(teamName)

    val shouldShowDuplicateError: Boolean
        get() = errorMessage?.contains("중복") == true

    val canSubmit: Boolean
        get() = hasValidLength && containsOnlyAllowedCharacters && doesNotContainSpecialCharacters && !isLoading
}

data class TeamJoinUiState(
    val inviteCode: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSubmit: Boolean
        get() = inviteCode.length == INVITE_CODE_LENGTH && INVITE_CODE_REGEX.matches(inviteCode) && !isLoading
}

@Composable
fun TeamCreateScreen(
    state: TeamCreateUiState,
    onTeamNameChange: (String) -> Unit,
    onCreateTeam: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White),
    ) {
        TopNavigationBar(
            title = "팀 생성",
            onBack = onBack,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(top = 8.dp),
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        ) {
            Text(
                text = "팀 이름을\n입력해주세요",
                style = RunpamineTypography.Header2.copy(lineHeight = 30.sp),
                color = Color.Black,
                modifier = Modifier.padding(top = 28.dp),
            )
            TeamTextField(
                value = state.teamName,
                onValueChange = onTeamNameChange,
                placeholder = "예: 팀 커브볼",
                isError = state.errorMessage != null,
                leadingContent = {
                    Image(
                        painter = painterResource(Res.drawable.icon_team),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        colorFilter = ColorFilter.tint(RunpamineColors.TextSecondary.copy(alpha = 0.7f)),
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                fieldHeight = 62,
            )
            Column(
                modifier = Modifier.padding(top = 14.dp, start = 8.dp),
            ) {
                ValidationRuleRow(text = "2-10자 이내", isValid = state.hasValidLength)
                ValidationRuleRow(
                    text = "한글, 영문, 숫자 사용 가능",
                    isValid = state.containsOnlyAllowedCharacters,
                    modifier = Modifier.padding(top = 9.dp),
                )
                ValidationRuleRow(
                    text = "특수문자 사용 불가",
                    isValid = state.doesNotContainSpecialCharacters,
                    modifier = Modifier.padding(top = 9.dp),
                )
                if (state.shouldShowDuplicateError) {
                    ValidationRuleRow(
                        text = "중복된 팀 이름입니다.",
                        isValid = false,
                        modifier = Modifier.padding(top = 9.dp),
                    )
                } else if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage,
                        style = RunpamineTypography.Body2,
                        color = RunpamineColors.Danger,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            PrimaryButton(
                title = "팀 생성하기",
                onClick = { onCreateTeam(state.teamName.trim()) },
                enabled = state.canSubmit,
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth().padding(bottom = 34.dp),
            )
        }
    }
}

@Composable
fun TeamJoinScreen(
    state: TeamJoinUiState,
    onInviteCodeChange: (String) -> Unit,
    onJoinTeam: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White),
    ) {
        TopNavigationBar(
            title = "팀 참가",
            onBack = onBack,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(top = 8.dp),
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        ) {
            Text(
                text = "팀 초대 코드를\n입력해주세요",
                style = RunpamineTypography.Header1.copy(fontSize = 29.sp, lineHeight = 36.sp, fontWeight = FontWeight.Black),
                color = Color.Black,
                modifier = Modifier.padding(top = 28.dp),
            )
            TeamTextField(
                value = state.inviteCode,
                onValueChange = onInviteCodeChange,
                placeholder = "예: ABCDEF",
                isError = state.errorMessage != null,
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                fieldHeight = 64,
                keyboardOptions =
                    KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        keyboardType = KeyboardType.Ascii,
                    ),
            )
            if (state.errorMessage != null) {
                Row(
                    modifier = Modifier.padding(top = 18.dp, start = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "×",
                        color = RunpamineColors.Danger,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = state.errorMessage.ifBlank { "팀 코드가 올바르지 않습니다." },
                        style = RunpamineTypography.Body1,
                        color = RunpamineColors.Danger,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            PrimaryButton(
                title = "팀 참가하기",
                onClick = { onJoinTeam(state.inviteCode.trim()) },
                enabled = state.canSubmit,
                isLoading = state.isLoading,
                modifier = Modifier.fillMaxWidth().padding(bottom = 34.dp),
            )
        }
    }
}

@Composable
private fun TeamTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean,
    modifier: Modifier = Modifier,
    fieldHeight: Int,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    leadingContent: (@Composable () -> Unit)? = null,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier =
            modifier
                .height(fieldHeight.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(
                    width = 2.dp,
                    color = if (isError) RunpamineColors.Danger else RunpamineColors.Primary,
                    shape = RoundedCornerShape(8.dp),
                ),
        textStyle = RunpamineTypography.Title2.copy(color = Color.Black),
        singleLine = true,
        cursorBrush = SolidColor(RunpamineColors.Primary),
        keyboardOptions = keyboardOptions,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = if (leadingContent == null) 20.dp else 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leadingContent != null) {
                    leadingContent()
                    Spacer(modifier = Modifier.size(12.dp))
                }
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = RunpamineTypography.Title2,
                            color = RunpamineColors.TextSecondary.copy(alpha = 0.72f),
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}

@Preview
@Composable
private fun TeamCreatePreview() {
    RunpamineTheme {
        TeamCreateScreen(
            state = TeamCreateUiState(teamName = "런앤런"),
            onTeamNameChange = {},
            onCreateTeam = {},
            onBack = {},
        )
    }
}

@Preview
@Composable
private fun TeamJoinPreview() {
    RunpamineTheme {
        TeamJoinScreen(
            state = TeamJoinUiState(inviteCode = "ABCDEF"),
            onInviteCodeChange = {},
            onJoinTeam = {},
            onBack = {},
        )
    }
}

private const val TEAM_NAME_MIN_LENGTH = 2
private const val TEAM_NAME_MAX_LENGTH = 10
private const val INVITE_CODE_LENGTH = 6
private val TEAM_NAME_ALLOWED_REGEX = Regex("^[가-힣a-zA-Z0-9]+$")
private val TEAM_NAME_SPECIAL_CHARACTER_REGEX = Regex("[^가-힣a-zA-Z0-9\\s]")
private val INVITE_CODE_REGEX = Regex("^[a-zA-Z0-9]+$")
