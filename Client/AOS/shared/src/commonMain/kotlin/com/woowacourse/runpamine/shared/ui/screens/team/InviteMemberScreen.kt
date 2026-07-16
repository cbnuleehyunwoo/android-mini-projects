package com.woowacourse.runpamine.shared.ui.screens.team

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
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
import com.woowacourse.runpamine.shared.ui.components.TopNavigationBar
import com.woowacourse.runpamine.shared.ui.theme.RunpamineColors
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTheme
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTypography

@Composable
fun InviteMemberScreen(
    inviteCode: String,
    onCopyCode: () -> Unit,
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
            title = "팀 초대",
            onBack = onBack,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(top = 8.dp),
        )
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "아래 코드를 공유하면 누구든 팀에 참여할 수 있어요",
                style = RunpamineTypography.Body1.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                color = Color.Black,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.padding(top = 44.dp),
            )
            InviteCodeCard(
                inviteCode = inviteCode,
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
            )
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp)
                        .height(49.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(Color(0xFFEDF2FF))
                        .clickable(role = Role.Button, onClick = onCopyCode),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CopyIcon(
                    modifier = Modifier.size(20.dp),
                    color = RunpamineColors.Primary,
                )
                Spacer(modifier = Modifier.width(9.dp))
                Text(
                    text = "코드 복사",
                    style = RunpamineTypography.Body1.copy(fontSize = 18.sp, fontWeight = FontWeight.Medium),
                    color = RunpamineColors.Primary,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun InviteCodeCard(
    inviteCode: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .height(102.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF2F4F8))
                .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        inviteCode.trim().take(MAX_INVITE_CODE_LENGTH).forEach { character ->
            Box(
                modifier =
                    Modifier
                        .size(width = 46.dp, height = 54.dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(13.dp),
                            ambientColor = Color.Black.copy(alpha = 0.09f),
                        ).clip(RoundedCornerShape(13.dp))
                        .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = character.toString(),
                    style =
                        RunpamineTypography.Header2.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    color = if (character.isLetter()) RunpamineColors.Primary else RunpamineColors.TextPrimary,
                )
            }
        }
    }
}

@Composable
private fun CopyIcon(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 1.8.dp.toPx()
        val cornerRadius = 2.5.dp.toPx()
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.27f, size.height * 0.09f),
            size = Size(size.width * 0.62f, size.height * 0.62f),
            cornerRadius = CornerRadius(cornerRadius),
            style = Stroke(width = strokeWidth),
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width * 0.09f, size.height * 0.29f),
            size = Size(size.width * 0.62f, size.height * 0.62f),
            cornerRadius = CornerRadius(cornerRadius),
            style = Stroke(width = strokeWidth),
        )
    }
}

@Preview
@Composable
private fun InviteMemberScreenPreview() {
    RunpamineTheme {
        InviteMemberScreen(
            inviteCode = "A1B2C3",
            onCopyCode = {},
            onBack = {},
        )
    }
}

private const val MAX_INVITE_CODE_LENGTH = 6
