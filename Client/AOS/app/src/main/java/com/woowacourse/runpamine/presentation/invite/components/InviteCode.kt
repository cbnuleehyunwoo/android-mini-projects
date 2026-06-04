package com.woowacourse.runpamine.presentation.invite.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.ui.theme.Gray40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

private const val CODE_LENGTH = 6

@Composable
fun InviteCode(
    code: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(CODE_LENGTH) { index ->
            CodeBox(character = code.getOrNull(index))
        }
    }
}

@Composable
private fun CodeBox(
    character: Char?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = 48.dp, height = 56.dp)
            .background(
                color = Gray40.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = character?.toString() ?: "",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.Black,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InviteCodePreview() {
    RunpamineTheme {
        InviteCode(code = "AB12CD")
    }
}
