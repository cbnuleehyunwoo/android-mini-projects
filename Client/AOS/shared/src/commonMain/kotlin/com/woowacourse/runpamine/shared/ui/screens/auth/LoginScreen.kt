package com.woowacourse.runpamine.shared.ui.screens.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.shared.generated.resources.Res
import com.woowacourse.runpamine.shared.generated.resources.character_no_bg
import com.woowacourse.runpamine.shared.generated.resources.icon_google_login
import com.woowacourse.runpamine.shared.ui.theme.RunpamineColors
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTheme
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTypography
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    supportsAppleLogin: Boolean,
    onGoogleLogin: () -> Unit,
    onAppleLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(Color.White)) {
        Column(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.character_no_bg),
                contentDescription = "런파민 캐릭터",
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(Modifier.height(34.dp))
            Text(
                text = "RUNPAMINE",
                style = RunpamineTypography.LoginTitle,
                color = RunpamineColors.TextPrimary,
                maxLines = 1,
            )
            Spacer(Modifier.height(50.dp))

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GoogleLoginButton(
                    isLoading = isLoading,
                    onClick = onGoogleLogin,
                    modifier = Modifier.widthIn(max = LoginButtonWidth).fillMaxWidth(),
                )
                if (supportsAppleLogin) {
                    AppleLoginButton(
                        isLoading = isLoading,
                        onClick = onAppleLogin,
                        modifier = Modifier.widthIn(max = LoginButtonWidth).fillMaxWidth(),
                    )
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = RunpamineTypography.Body2,
                    color = RunpamineColors.Danger,
                    modifier = Modifier.padding(top = 14.dp, start = 32.dp, end = 32.dp),
                )
            }
        }
    }
}

@Composable
private fun GoogleLoginButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .height(LoginButtonHeight)
                .clickable(enabled = !isLoading, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(Res.drawable.icon_google_login),
            contentDescription = "Google 계정으로 로그인",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds,
        )
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.72f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = RunpamineColors.TextPrimary,
                    strokeWidth = 2.5.dp,
                )
            }
        }
    }
}

@Composable
private fun AppleLoginButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .height(LoginButtonHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
                .clickable(enabled = !isLoading, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            AppleMark()
            Text(
                text = "Apple로 로그인",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.72f)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp,
                )
            }
        }
    }
}

@Composable
private fun AppleMark(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val width = size.width
        val height = size.height
        val body =
            Path().apply {
                moveTo(width * 0.51f, height * 0.29f)
                cubicTo(width * 0.40f, height * 0.20f, width * 0.21f, height * 0.25f, width * 0.15f, height * 0.43f)
                cubicTo(width * 0.06f, height * 0.68f, width * 0.25f, height * 0.94f, width * 0.38f, height * 0.94f)
                cubicTo(width * 0.48f, height * 0.94f, width * 0.51f, height * 0.88f, width * 0.60f, height * 0.88f)
                cubicTo(width * 0.69f, height * 0.88f, width * 0.73f, height * 0.94f, width * 0.82f, height * 0.94f)
                cubicTo(width * 0.92f, height * 0.94f, width * 1.02f, height * 0.76f, width * 0.98f, height * 0.62f)
                cubicTo(width * 0.82f, height * 0.56f, width * 0.79f, height * 0.36f, width * 0.92f, height * 0.27f)
                cubicTo(width * 0.80f, height * 0.17f, width * 0.65f, height * 0.20f, width * 0.51f, height * 0.29f)
                close()
            }
        drawPath(body, Color.White)

        val leaf =
            Path().apply {
                moveTo(width * 0.52f, height * 0.22f)
                cubicTo(width * 0.53f, height * 0.08f, width * 0.64f, 0f, width * 0.75f, height * 0.01f)
                cubicTo(width * 0.75f, height * 0.13f, width * 0.66f, height * 0.23f, width * 0.52f, height * 0.22f)
                close()
            }
        drawPath(leaf, Color.White)
    }
}

private val LoginButtonWidth = 308.dp
private val LoginButtonHeight = 56.dp

@Preview
@Composable
private fun LoginScreenPreview() {
    RunpamineTheme {
        LoginScreen(
            isLoading = false,
            errorMessage = null,
            supportsAppleLogin = true,
            onGoogleLogin = {},
            onAppleLogin = {},
        )
    }
}
