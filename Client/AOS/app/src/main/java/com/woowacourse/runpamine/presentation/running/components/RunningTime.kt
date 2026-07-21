package com.woowacourse.runpamine.presentation.running.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
fun RunningTime(
    time: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.running_time),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            fontWeight = FontWeight.Medium,
            color = Color.Black,
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = time,
            style = MaterialTheme.typography.displayLarge.copy(fontSize = 28.sp),
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RunningTimePreview() {
    RunpamineTheme {
        RunningTime(
            time = "28:45",
            modifier = Modifier.padding(16.dp),
        )
    }
}
