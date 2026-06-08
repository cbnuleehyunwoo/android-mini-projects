package com.woowacourse.runpamine.presentation.running.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun RunningDistance(
    distance: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.running_distance),
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 19.sp, lineHeight = 24.sp),
            color = Color(0xFF4C4546),
        )
        Row(
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = distance,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 72.sp, lineHeight = 82.sp),
                fontWeight = FontWeight.Black,
                color = Color.Black,
            )
            Text(
                text = stringResource(R.string.running_distance_unit),
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp),
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp, lineHeight = 38.sp),
                fontWeight = FontWeight.Black,
                color = Color(0xFF5F5A5B),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393)
@Composable
private fun RunningDistancePreview() {
    RunpamineTheme {
        RunningDistance(
            distance = "3.25",
            modifier = Modifier.padding(16.dp),
        )
    }
}
