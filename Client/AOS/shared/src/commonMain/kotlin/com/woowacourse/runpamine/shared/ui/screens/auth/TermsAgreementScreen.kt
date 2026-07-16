package com.woowacourse.runpamine.shared.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.shared.ui.components.CheckBox
import com.woowacourse.runpamine.shared.ui.components.PrimaryButton
import com.woowacourse.runpamine.shared.ui.components.TopNavigationBar
import com.woowacourse.runpamine.shared.ui.theme.RunpamineColors
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTheme
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTypography

@Composable
fun TermsAgreementScreen(
    serviceTermsAccepted: Boolean,
    privacyPolicyAccepted: Boolean,
    onToggleAll: () -> Unit,
    onToggleServiceTerms: () -> Unit,
    onTogglePrivacyPolicy: () -> Unit,
    onOpenServiceTerms: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val allAccepted = serviceTermsAccepted && privacyPolicyAccepted

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White)
                .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        TopNavigationBar(
            title = "회원가입",
            onBack = onBack,
            modifier = Modifier.padding(horizontal = 18.dp).padding(top = 8.dp),
        )

        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 32.dp),
        ) {
            Text(
                text = "서비스 이용을 위해 약관에 동의해주세요",
                style = RunpamineTypography.Body1.copy(fontWeight = FontWeight.SemiBold),
                color = RunpamineColors.TextSecondary,
                modifier = Modifier.padding(top = 44.dp),
            )

            AllAgreementCard(
                checked = allAccepted,
                onToggle = onToggleAll,
                modifier = Modifier.padding(top = 42.dp),
            )

            Column(
                modifier = Modifier.padding(top = 34.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                TermsAgreementRow(
                    title = "서비스 이용약관 동의",
                    checked = serviceTermsAccepted,
                    onToggle = onToggleServiceTerms,
                    onOpenDetail = onOpenServiceTerms,
                )
                TermsAgreementRow(
                    title = "개인정보처리방침 동의",
                    checked = privacyPolicyAccepted,
                    onToggle = onTogglePrivacyPolicy,
                    onOpenDetail = onOpenPrivacyPolicy,
                )
            }

            Spacer(Modifier.weight(1f))

            PrimaryButton(
                title = "가입하기",
                onClick = onComplete,
                enabled = allAccepted,
                modifier = Modifier.padding(bottom = 34.dp),
            )
        }
    }
}

@Composable
private fun AllAgreementCard(
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(78.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(RunpamineColors.Surface)
                .border(1.dp, RunpamineColors.Border, RoundedCornerShape(8.dp))
                .clickable(role = Role.Checkbox, onClick = onToggle)
                .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CheckBox(checked = checked, onClick = onToggle, size = 28)
        Text(
            text = "전체 동의",
            style = RunpamineTypography.Body1.copy(fontWeight = FontWeight.SemiBold),
            color = RunpamineColors.TextPrimary,
        )
    }
}

@Composable
private fun TermsAgreementRow(
    title: String,
    checked: Boolean,
    onToggle: () -> Unit,
    onOpenDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CheckBox(checked = checked, onClick = onToggle, size = 27)

        Row(
            modifier = Modifier.weight(1f).clickable(role = Role.Button, onClick = onOpenDetail),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = RunpamineTypography.Body1.copy(fontWeight = FontWeight.SemiBold),
                    color = RunpamineColors.TextPrimary,
                )
                Text(
                    text = "(필수)",
                    style = RunpamineTypography.Body2,
                    color = RunpamineColors.Danger,
                )
            }
            Text(
                text = "›",
                color = RunpamineColors.TextSecondary.copy(alpha = 0.65f),
                fontSize = 30.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Preview
@Composable
private fun TermsAgreementScreenPreview() {
    RunpamineTheme {
        TermsAgreementScreen(
            serviceTermsAccepted = true,
            privacyPolicyAccepted = false,
            onToggleAll = {},
            onToggleServiceTerms = {},
            onTogglePrivacyPolicy = {},
            onOpenServiceTerms = {},
            onOpenPrivacyPolicy = {},
            onBack = {},
            onComplete = {},
        )
    }
}
