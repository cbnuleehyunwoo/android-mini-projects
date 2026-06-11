package com.woowacourse.runpamine.presentation.termsagreement

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.presentation.component.ScreenTopBar
import com.woowacourse.runpamine.presentation.termsagreement.components.AgreementRow
import com.woowacourse.runpamine.presentation.termsagreement.components.AllAgreementCard
import com.woowacourse.runpamine.ui.theme.Blue40
import com.woowacourse.runpamine.ui.theme.Gray40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun TermsAgreementScreen(
    onBackClick: () -> Unit,
    onJoinClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var serviceTermsAgreed by rememberSaveable { mutableStateOf(false) }
    var privacyPolicyAgreed by rememberSaveable { mutableStateOf(false) }
    val allAgreed = serviceTermsAgreed && privacyPolicyAgreed
    val uriHandler = LocalUriHandler.current

    TermsAgreementContent(
        serviceTermsAgreed = serviceTermsAgreed,
        privacyPolicyAgreed = privacyPolicyAgreed,
        onBackClick = onBackClick,
        onAllAgreementClick = {
            val nextValue = !allAgreed
            serviceTermsAgreed = nextValue
            privacyPolicyAgreed = nextValue
        },
        onServiceTermsClick = { serviceTermsAgreed = !serviceTermsAgreed },
        onPrivacyPolicyClick = { privacyPolicyAgreed = !privacyPolicyAgreed },
        onServiceTermsOpenClick = { uriHandler.openUri(SERVICE_TERMS_URL) },
        onPrivacyPolicyOpenClick = { uriHandler.openUri(PRIVACY_POLICY_URL) },
        onJoinClick = {
            if (allAgreed) {
                onJoinClick()
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun TermsAgreementContent(
    serviceTermsAgreed: Boolean,
    privacyPolicyAgreed: Boolean,
    onBackClick: () -> Unit,
    onAllAgreementClick: () -> Unit,
    onServiceTermsClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onServiceTermsOpenClick: () -> Unit,
    onPrivacyPolicyOpenClick: () -> Unit,
    onJoinClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .navigationBarsPadding(),
    ) {
        ScreenTopBar(
            title = stringResource(R.string.terms_agreement_title),
            onBackClick = onBackClick,
        )
        Spacer(modifier = Modifier.height(82.dp))
        Text(
            text = stringResource(R.string.terms_agreement_description),
            color = Gray40,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(modifier = Modifier.height(64.dp))
        AllAgreementCard(
            checked = serviceTermsAgreed && privacyPolicyAgreed,
            onClick = onAllAgreementClick,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(40.dp))
        AgreementRow(
            title = stringResource(R.string.terms_agreement_service_terms),
            checked = serviceTermsAgreed,
            onCheckedClick = onServiceTermsClick,
            onOpenClick = onServiceTermsOpenClick,
        )
        Spacer(modifier = Modifier.height(24.dp))
        AgreementRow(
            title = stringResource(R.string.terms_agreement_privacy_policy),
            checked = privacyPolicyAgreed,
            onCheckedClick = onPrivacyPolicyClick,
            onOpenClick = onPrivacyPolicyOpenClick,
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = onJoinClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(64.dp),
            shape = RoundedCornerShape(10.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Blue40,
                    disabledContainerColor = Blue40,
                ),
        ) {
            Text(
                text = stringResource(R.string.terms_agreement_next),
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.ExtraBold,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

private const val SERVICE_TERMS_URL =
    "https://sheer-mimosa-20f.notion.site/37958b8d8e6c8050b988fcc4e6279e25?source=copy_link"
private const val PRIVACY_POLICY_URL =
    "https://sheer-mimosa-20f.notion.site/37958b8d8e6c80cdb6b8c29d6d6935f5?source=copy_link"

@Preview(showBackground = true, widthDp = 1000)
@Composable
private fun TermsAgreementScreenPreview() {
    RunpamineTheme {
        TermsAgreementContent(
            serviceTermsAgreed = true,
            privacyPolicyAgreed = false,
            onBackClick = {},
            onAllAgreementClick = {},
            onServiceTermsClick = {},
            onPrivacyPolicyClick = {},
            onServiceTermsOpenClick = {},
            onPrivacyPolicyOpenClick = {},
            onJoinClick = {},
        )
    }
}
