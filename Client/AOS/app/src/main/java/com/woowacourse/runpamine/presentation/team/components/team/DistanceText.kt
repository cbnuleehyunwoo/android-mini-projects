package com.woowacourse.runpamine.presentation.team.components.team

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun DistanceText(
    distance: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = distance,
            style =
                MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 30.sp,
                    lineHeight = 48.sp,
                ),
            fontWeight = FontWeight.Black,
            color = Color.Black,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "km",
            modifier = Modifier.padding(bottom = 5.dp),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = Color(0xFF666666),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DistanceTextPreview() {
    RunpamineTheme {
        DistanceText(
            distance = "12.3",
        )
    }
}
