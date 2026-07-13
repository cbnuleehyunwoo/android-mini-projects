package com.woowacourse.runpamine.presentation.invite.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

private const val CODE_LENGTH = 6

@Composable
fun InviteCode(
    code: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(102.dp)
                .background(Color(0xFFF2F5FA), RoundedCornerShape(24.dp)),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
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
        modifier =
            modifier
                .size(width = 46.dp, height = 54.dp)
                .shadow(12.dp, RoundedCornerShape(13.dp))
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(13.dp),
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = character?.toString() ?: "",
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 24.sp),
            fontWeight = FontWeight.Bold,
            color = if (character?.isLetter() == true) Color(0xFF0058FF) else Color(0xFF111827),
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
