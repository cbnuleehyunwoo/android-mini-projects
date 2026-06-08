package com.woowacourse.runpamine.presentation.running.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_time),
                contentDescription = stringResource(R.string.running_time),
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.running_time),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp, lineHeight = 23.sp),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4C4546),
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = time,
            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 38.sp, lineHeight = 46.sp),
            fontWeight = FontWeight.Black,
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
