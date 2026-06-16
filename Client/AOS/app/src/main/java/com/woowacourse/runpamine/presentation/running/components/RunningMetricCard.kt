package com.woowacourse.runpamine.presentation.running.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun RunningMetricCard(
    @DrawableRes iconResId: Int,
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
    shadowElevation: Dp = 16.dp,
) {
    Surface(
        modifier = modifier.heightIn(min = 129.dp),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        shadowElevation = shadowElevation,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 24.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = title,
                    modifier = Modifier.size(22.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    maxLines = 1,
                    softWrap = false,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp, lineHeight = 23.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4C4546),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = value,
                    maxLines = 1,
                    softWrap = false,
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp, lineHeight = 42.sp),
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                )
                Text(
                    text = unit,
                    modifier = Modifier.padding(start = 3.dp, bottom = 5.dp),
                    maxLines = 1,
                    softWrap = false,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 20.sp),
                    fontWeight = FontWeight.Bold,
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
            iconResId = R.drawable.ic_pace,
            title = "평균 페이스",
            value = "15'30\"",
            unit = "/km",
            modifier = Modifier.padding(16.dp),
        )
    }
}
