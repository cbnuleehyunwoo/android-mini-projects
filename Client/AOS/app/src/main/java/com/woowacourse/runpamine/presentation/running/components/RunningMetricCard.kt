package com.woowacourse.runpamine.presentation.running.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun RunningMetricCard(
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    shadowElevation: Dp = 16.dp,
    summaryStyle: Boolean = false,
) {
    Surface(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(if (summaryStyle) 10.dp else 12.dp),
        color = Color.White,
        shadowElevation = shadowElevation,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = if (summaryStyle) 24.dp else 22.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    maxLines = 1,
                    softWrap = false,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = if (summaryStyle) 13.sp else 14.sp),
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF4C4546),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = value,
                    maxLines = 1,
                    softWrap = false,
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = if (summaryStyle) 27.sp else 28.sp),
                    fontWeight = if (summaryStyle) FontWeight.Black else FontWeight.Bold,
                    color = Color.Black,
                )
                Text(
                    text = unit,
                    modifier = Modifier.padding(start = 3.dp, bottom = 3.dp),
                    maxLines = 1,
                    softWrap = false,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                    fontWeight = if (summaryStyle) FontWeight.Black else FontWeight.Medium,
                    color = Color(0xFF5F5A5B),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 200)
@Composable
private fun RunningMetricCardPreview() {
    RunpamineTheme {
        RunningMetricCard(
            title = "평균 페이스",
            value = "15'30\"",
            unit = "/km",
            modifier = Modifier.padding(16.dp),
        )
    }
}
