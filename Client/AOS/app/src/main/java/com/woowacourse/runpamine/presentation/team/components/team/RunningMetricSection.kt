package com.woowacourse.runpamine.presentation.team.components.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.ui.theme.Pretendard
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun RunningMetricSection(
    distance: String,
    time: String,
    pace: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.Start,
    ) {
        MetricText(
            label = "거리",
            value = distance,
        )
        MetricText(
            label = "시간",
            value = time,
        )
        MetricText(
            label = "페이스",
            value = pace,
        )
    }
}

@Composable
private fun MetricText(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.width(48.dp),
            style =
                TextStyle(
                    fontFamily = Pretendard,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
            color = Color(0xFF94A3B8),
            maxLines = 1,
        )
        Spacer(
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style =
                TextStyle(
                    fontFamily = Pretendard,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
            color = Color(0xFF121C2B),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true, widthDp = 160)
@Composable
private fun RunningMetricSectionPreview() {
    RunpamineTheme {
        RunningMetricSection(
            modifier = Modifier.padding(16.dp),
            distance = "12.3km",
            time = "23:32",
            pace = "5'30\"",
        )
    }
}
