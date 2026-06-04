package com.woowacourse.runpamine.presentation.invite.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun CopyCodeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
    ) {
        Text(
            text = stringResource(R.string.invite_copy_code),
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            modifier = Modifier.padding(vertical = 8.dp),
        )
    }
}

@Preview
@Composable
private fun CopyCodeButtonPreview() {
    RunpamineTheme {
        CopyCodeButton(onClick = {})
    }
}
