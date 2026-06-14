package com.woowacourse.runpamine.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.Pretendard

@Composable
fun RunpamineLogo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.img_splash),
            contentDescription = "런파민 로고",
            modifier = Modifier.size(200.dp),
            tint = Color.Unspecified,
        )

        Text(
            text = "Runpamine",
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = Pretendard,
        )
    }
}

@Preview
@Composable
private fun RunpamineLogoPreview() {
    RunpamineLogo()
}
