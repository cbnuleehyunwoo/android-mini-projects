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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.RunpamineBrand

@Composable
fun RunpamineLogo(
    modifier: Modifier = Modifier,
    characterSize: Dp = 200.dp,
    titleFontSize: TextUnit = 44.sp,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(34.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.img_character_no_bg),
            contentDescription = "런파민 로고",
            modifier = Modifier.size(characterSize),
            tint = Color.Unspecified,
        )

        Text(
            text = "RUNPAMINE",
            color = Color(0xFF121C2B),
            fontSize = titleFontSize,
            fontFamily = RunpamineBrand,
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun RunpamineLogoPreview() {
    RunpamineLogo()
}
