package com.woowacourse.runpamine.presentation.team.components.team

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
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
import com.woowacourse.runpamine.ui.theme.Blue40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun TeamSummaryCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(12.dp)

    Column(
        modifier =
            modifier
                .height(90.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = cardShape,
                    ambientColor = Color.Transparent,
                    spotColor = Color.Black.copy(alpha = 0.8f),
                ).background(
                    color = Color.White,
                    shape = cardShape,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = Blue40,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6F7B91),
        )
    }
}

@Preview(showBackground = true, widthDp = 150)
@Composable
private fun TeamSummaryCardPreview() {
    RunpamineTheme {
        TeamSummaryCard(
            value = "324 km",
            label = "팀 총 거리",
        )
    }
}
