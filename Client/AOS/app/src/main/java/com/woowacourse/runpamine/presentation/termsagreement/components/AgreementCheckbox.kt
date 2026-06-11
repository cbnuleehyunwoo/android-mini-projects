package com.woowacourse.runpamine.presentation.termsagreement.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.ui.theme.Blue40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun AgreementCheckbox(
    checked: Boolean,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 36.dp,
) {
    Box(
        modifier =
            modifier
                .size(size)
                .clip(RoundedCornerShape(6.dp))
                .background(if (checked) Blue40 else Color(0xFFE5E9EF))
                .border(
                    width = if (checked) 0.dp else 1.dp,
                    color = Color(0xFFD1D7E0),
                    shape = RoundedCornerShape(6.dp),
                ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(size * 0.68f),
            )
        }
    }
}

@Preview
@Composable
private fun AgreementCheckboxPreview() {
    RunpamineTheme {
        AgreementCheckbox(
            checked = true,
        )
    }
}
