package com.woowacourse.runpamine.presentation.termsagreement.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun AgreementRow(
    title: String,
    checked: Boolean,
    onCheckedClick: () -> Unit,
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AgreementCheckbox(
            checked = checked,
            modifier =
                Modifier
                    .clickable(
                        role = Role.Checkbox,
                        onClick = onCheckedClick,
                    ).size(24.dp),
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 24.dp),
        ) {
            Text(
                text = title,
                color = Color(0xFF111827),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.terms_agreement_required),
                color = Color(0xFFFF2D2D),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = stringResource(R.string.terms_agreement_open_content_description, title),
            tint = Color(0xFF9AA3B2),
            modifier =
                Modifier
                    .size(34.dp)
                    .clickable(onClick = onOpenClick),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AgreementRowPreview() {
    RunpamineTheme {
        AgreementRow(
            title = stringResource(R.string.terms_agreement_service_terms),
            checked = true,
            onCheckedClick = {},
            onOpenClick = {},
        )
    }
}
