package com.woowacourse.runpamine.presentation.ranking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.ui.theme.Blue40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun RankBadge(
    rank: Int?,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(28.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) Blue40 else Color(0xFFA1AAB8)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = rank?.toString() ?: "-",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
        )
    }
}

@Preview
@Composable
private fun RankBadgePreview() {
    RunpamineTheme {
        RankBadge(
            rank = 1,
            selected = true,
        )
    }
}
