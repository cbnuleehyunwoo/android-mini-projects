package com.woowacourse.runpamine.presentation.team.components.team

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun CalorieMetricCard(
    calories: String,
    modifier: Modifier = Modifier,
) {
    val cardShape = RoundedCornerShape(12.dp)

    Column(
        modifier =
            modifier
                .background(
                    color = Color(0xFFFFF4EF),
                    shape = cardShape,
                ).border(
                    width = 1.dp,
                    color = Color(0xFFFED7AA),
                    shape = cardShape,
                ).padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_kcal),
            contentDescription = null,
            tint = Color(0xFFEA580C),
            modifier = Modifier.size(width = 13.dp, height = 15.dp),
        )
        Text(
            text = calories,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
            color = Color(0xFFEA580C),
        )
        Text(
            text = "kcal",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = Color(0xFFEA580C),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CalorieMetricCardPreview() {
    RunpamineTheme {
        CalorieMetricCard(
            calories = "200",
            modifier = Modifier.padding(16.dp),
        )
    }
}
