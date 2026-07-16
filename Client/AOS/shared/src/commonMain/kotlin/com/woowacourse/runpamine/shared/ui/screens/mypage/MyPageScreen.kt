package com.woowacourse.runpamine.shared.ui.screens.mypage

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.shared.ui.components.RunpamineConfirmationDialog
import com.woowacourse.runpamine.shared.ui.theme.RunpamineColors
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTheme
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTypography

@Composable
fun MyPageScreen(
    nickname: String,
    appVersion: String,
    onClose: () -> Unit,
    onOpenNicknameChange: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTerms: () -> Unit,
    modifier: Modifier = Modifier,
    isBusy: Boolean = false,
    errorMessage: String? = null,
) {
    var visibleDialog by remember { mutableStateOf<MyPageDialog?>(null) }

    Column(modifier = modifier.fillMaxSize().background(Color.White)) {
        MyPageTopBar(
            onClose = onClose,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) {
            item {
                ProfileHeader(
                    nickname = nickname,
                    modifier = Modifier.fillMaxWidth().padding(top = 26.dp),
                )
            }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SectionTitle("계정 설정")
                    SettingsRow(
                        glyph = "✎",
                        title = "닉네임 변경",
                        subtitle = "닉네임을 변경할 수 있습니다.",
                        enabled = !isBusy,
                        onClick = onOpenNicknameChange,
                    )
                    SettingsRow(
                        glyph = "↪",
                        title = "로그아웃",
                        subtitle = "계정에서 로그아웃합니다",
                        isDestructive = true,
                        enabled = !isBusy,
                        onClick = { visibleDialog = MyPageDialog.Logout },
                    )
                    SettingsRow(
                        glyph = "×",
                        title = "회원탈퇴",
                        subtitle = "계정을 삭제합니다",
                        isDestructive = true,
                        enabled = !isBusy,
                        onClick = { visibleDialog = MyPageDialog.DeleteAccount },
                    )

                    if (isBusy) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = RunpamineColors.Primary,
                                strokeWidth = 2.dp,
                            )
                            Text(
                                text = "요청을 처리하고 있어요",
                                style = RunpamineTypography.Caption1,
                                color = RunpamineColors.TextSecondary,
                            )
                        }
                    }

                    if (!errorMessage.isNullOrBlank()) {
                        Text(
                            text = errorMessage,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            style = RunpamineTypography.Caption1,
                            color = RunpamineColors.Danger,
                        )
                    }

                    SectionTitle("약관 및 정책", Modifier.padding(top = 4.dp))
                    SettingsRow(
                        glyph = "◇",
                        title = "개인정보처리방침",
                        subtitle = "개인정보 수집 및 이용에 대한 안내",
                        enabled = !isBusy,
                        onClick = onOpenPrivacyPolicy,
                    )
                    SettingsRow(
                        glyph = "□",
                        title = "이용약관",
                        subtitle = "서비스 이용에 관한 약관을 확인하세요",
                        enabled = !isBusy,
                        onClick = onOpenTerms,
                    )

                    SectionTitle("기타", Modifier.padding(top = 4.dp))
                    AppInfoRow(appVersion = appVersion)
                }
            }
        }
    }

    when (visibleDialog) {
        MyPageDialog.Logout -> {
            RunpamineConfirmationDialog(
                title = "로그아웃",
                message = "정말 로그아웃 하시겠습니까?",
                dismissText = "취소",
                confirmText = "로그아웃",
                onDismiss = { visibleDialog = null },
                onConfirm = {
                    visibleDialog = null
                    onLogout()
                },
            )
        }
        MyPageDialog.DeleteAccount -> {
            RunpamineConfirmationDialog(
                title = "회원탈퇴",
                message = "회원 탈퇴 시 모든 정보가 삭제됩니다.\n정말 탈퇴하시겠습니까?",
                dismissText = "취소",
                confirmText = "회원탈퇴",
                onDismiss = { visibleDialog = null },
                onConfirm = {
                    visibleDialog = null
                    onDeleteAccount()
                },
                isDanger = true,
            )
        }
        null -> Unit
    }
}

@Composable
private fun MyPageTopBar(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth().height(60.dp)) {
        Text(
            text = "마이페이지",
            modifier = Modifier.align(Alignment.Center),
            style =
                RunpamineTypography.Title2.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            color = RunpamineColors.TextPrimary,
        )
        Box(
            modifier =
                Modifier
                    .align(Alignment.CenterEnd)
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onClose),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "×",
                style =
                    RunpamineTypography.Title2.copy(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                color = RunpamineColors.TextPrimary,
            )
        }
    }
}

@Composable
private fun ProfileHeader(
    nickname: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(RunpamineColors.Surface)
                    .border(1.dp, RunpamineColors.Border, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            PersonGlyph()
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = nickname,
            style = RunpamineTypography.Title2.copy(fontWeight = FontWeight.Bold),
            color = RunpamineColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PersonGlyph() {
    Canvas(Modifier.size(40.dp)) {
        val color = RunpamineColors.TextSecondary.copy(alpha = 0.58f)
        drawCircle(
            color = color,
            radius = 8.dp.toPx(),
            center = Offset(size.width / 2f, 10.dp.toPx()),
        )
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(4.dp.toPx(), 19.dp.toPx()),
            size = Size(32.dp.toPx(), 26.dp.toPx()),
            style = Stroke(width = 7.dp.toPx()),
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier.fillMaxWidth(),
        style =
            RunpamineTypography.Body2.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
            ),
        color = RunpamineColors.TextPrimary,
    )
}

@Composable
private fun SettingsRow(
    glyph: String,
    title: String,
    subtitle: String,
    enabled: Boolean,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, RunpamineColors.Border.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        SettingsGlyph(glyph = glyph, isDestructive = isDestructive, enabled = enabled)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                style = RunpamineTypography.Body2.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                color =
                    when {
                        !enabled -> RunpamineColors.TextSecondary.copy(alpha = 0.45f)
                        isDestructive -> RunpamineColors.Danger
                        else -> RunpamineColors.TextPrimary
                    },
            )
            Text(
                text = subtitle,
                style = RunpamineTypography.Caption1,
                color = RunpamineColors.TextSecondary.copy(alpha = if (enabled) 1f else 0.45f),
            )
        }
        Text(
            text = "›",
            style = RunpamineTypography.Title2.copy(fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
            color = RunpamineColors.TextSecondary.copy(alpha = if (enabled) 0.55f else 0.22f),
        )
    }
}

@Composable
private fun SettingsGlyph(
    glyph: String,
    isDestructive: Boolean,
    enabled: Boolean,
) {
    Box(
        modifier = Modifier.size(28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = glyph,
            style =
                RunpamineTypography.Title2.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            color =
                when {
                    !enabled -> RunpamineColors.TextSecondary.copy(alpha = 0.35f)
                    isDestructive -> RunpamineColors.Danger
                    else -> RunpamineColors.TextPrimary
                },
        )
    }
}

@Composable
private fun AppInfoRow(appVersion: String) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, RunpamineColors.Border.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(2.dp, RunpamineColors.TextPrimary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "i",
                style = RunpamineTypography.Caption1.copy(fontWeight = FontWeight.Black),
                color = RunpamineColors.TextPrimary,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = "앱 정보",
                style = RunpamineTypography.Body2.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                color = RunpamineColors.TextPrimary,
            )
            Text(
                text = "버전 $appVersion",
                style = RunpamineTypography.Caption1,
                color = RunpamineColors.TextSecondary,
            )
        }
    }
}

private enum class MyPageDialog {
    Logout,
    DeleteAccount,
}

@Preview
@Composable
private fun MyPageScreenPreview() {
    RunpamineTheme {
        MyPageScreen(
            nickname = "커비",
            appVersion = "1.0.0",
            onClose = {},
            onOpenNicknameChange = {},
            onLogout = {},
            onDeleteAccount = {},
            onOpenPrivacyPolicy = {},
            onOpenTerms = {},
        )
    }
}
