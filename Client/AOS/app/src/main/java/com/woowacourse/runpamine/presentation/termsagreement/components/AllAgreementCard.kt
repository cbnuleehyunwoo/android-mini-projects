package com.woowacourse.runpamine.presentation.termsagreement.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun AllAgreementCard(
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .height(64.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFF8FAFC))
                .border(
                    width = 1.dp,
                    color = Color(0xFFE1E7F0),
                    shape = RoundedCornerShape(18.dp),
                ).clickable(
                    role = Role.Checkbox,
                    onClick = onClick,
                ).padding(horizontal = 34.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        AgreementCheckbox(
            checked = checked,
            size = 24.dp,
        )
        Text(
            text = stringResource(R.string.terms_agreement_all),
            color = Color(0xFF111827),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview(widthDp = 500)
@Composable
private fun AllAgreementCardPreview() {
    RunpamineTheme {
        AllAgreementCard(
            checked = false,
            onClick = {},
        )
    }
}
