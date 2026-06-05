package com.woowacourse.runpamine.presentation.running

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun RunningScreen(
    modifier: Modifier = Modifier,
    onPauseClick: () -> Unit = {},
    onStopClick: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .safeDrawingPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            RunningDistance(
                distance = "5.20",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(22.dp))
            RunningTime(time = "28:45")
            Spacer(modifier = Modifier.height(28.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                RunningMetricCard(
                    iconResId = R.drawable.ic_pace,
                    title = stringResource(R.string.running_pace),
                    value = "5'30\"",
                    unit = "/km",
                    modifier = Modifier.weight(1f),
                )
                RunningMetricCard(
                    iconResId = R.drawable.ic_kcal,
                    title = stringResource(R.string.running_kcal),
                    value = "505",
                    unit = "kcal",
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(80.dp))
            RunningControls(
                onPauseClick = onPauseClick,
                onStopClick = onStopClick,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.weight(1.15f))
        }
    }
}

@Composable
private fun RunningDistance(
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

@Composable
private fun RunningTime(
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

@Composable
private fun RunningMetricCard(
    @DrawableRes iconResId: Int,
    title: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        shadowElevation = 16.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
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
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 34.sp, lineHeight = 42.sp),
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                )
                Text(
                    text = unit,
                    modifier = Modifier.padding(start = 3.dp, bottom = 5.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp, lineHeight = 20.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5F5A5B),
                )
            }
        }
    }
}

@Composable
private fun RunningControls(
    onPauseClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        RunningControlButton(
            text = stringResource(R.string.running_pause),
            iconResId = R.drawable.ic_pause,
            containerColor = Color.Black,
            contentColor = Color.White,
            onClick = onPauseClick,
            modifier = Modifier.weight(0.62f),
        )
        RunningControlButton(
            text = stringResource(R.string.running_stop),
            iconResId = R.drawable.ic_stop,
            containerColor = Color(0xFFFFF1F1),
            contentColor = Color(0xFFBA1A1A),
            borderColor = Color(0xFFFFB4AB),
            onClick = onStopClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RunningControlButton(
    text: String,
    @DrawableRes iconResId: Int,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
) {
    Row(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(10.dp))
            .then(
                if (borderColor == null) {
                    Modifier
                } else {
                    Modifier.border(1.dp, borderColor, RoundedCornerShape(10.dp))
                },
            )
            .clickable(
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = text,
            modifier = Modifier.size(18.dp),
            colorFilter = ColorFilter.tint(contentColor),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 22.sp),
            fontWeight = FontWeight.Black,
            color = contentColor,
        )
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun RunningScreenPreview() {
    RunpamineTheme {
        RunningScreen()
    }
}
