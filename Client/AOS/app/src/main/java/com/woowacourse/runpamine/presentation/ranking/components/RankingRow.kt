package com.woowacourse.runpamine.presentation.ranking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.presentation.ranking.model.RankingItem
import com.woowacourse.runpamine.ui.theme.Blue10
import com.woowacourse.runpamine.ui.theme.Blue40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun RankingRow(
    item: RankingItem,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (item.isMine) Blue10 else Color(0xFFF8F8F8))
                .height(52.dp)
                .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RankBadge(
            rank = item.rank,
            selected = item.isMine,
        )
        AutoResizeNameText(
            text = item.name,
            color = if (item.isMine) Blue40 else Color(0xFF384152),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = item.valueText,
            color = if (item.isMine) Blue40 else Color(0xFFA2ACBA),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun AutoResizeNameText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    var fontSize by remember(text) { mutableStateOf(RANKING_NAME_MAX_FONT_SIZE) }

    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        softWrap = false,
        modifier = modifier,
        onTextLayout = { textLayoutResult ->
            if (
                textLayoutResult.didOverflowWidth &&
                fontSize.value > RANKING_NAME_MIN_FONT_SIZE.value
            ) {
                fontSize = (fontSize.value - 1).sp
            }
        },
    )
}

@Preview
@Composable
private fun RankingRowPreview() {
    RunpamineTheme {
        RankingRow(
            item =
                RankingItem(
                    rank = 1,
                    name = "김영희",
                    valueText = "253.1 km",
                    isMine = true,
                ),
        )
    }
}

private val RANKING_NAME_MAX_FONT_SIZE = 14.sp
private val RANKING_NAME_MIN_FONT_SIZE = 10.sp
