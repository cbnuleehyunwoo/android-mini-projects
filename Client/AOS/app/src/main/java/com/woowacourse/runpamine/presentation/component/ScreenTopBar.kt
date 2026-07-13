package com.woowacourse.runpamine.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.ui.theme.RunpamineLayout
import com.woowacourse.runpamine.ui.theme.RunpamineTheme
import com.woowacourse.runpamine.ui.theme.RunpamineTypography

@Composable
fun ScreenTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(RunpamineLayout.NavigationHeight),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = RunpamineTypography.NavigationTitle,
            color = Color.Black,
        )
        if (showBackButton) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                tint = Color.Black,
                modifier =
                    Modifier
                        .align(Alignment.CenterStart)
                        .size(44.dp)
                        .padding(10.dp)
                        .clickable(onClick = onBackClick),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 500)
@Composable
private fun ScreenTopBarPreview() {
    RunpamineTheme {
        ScreenTopBar(
            title = "팀 생성",
            onBackClick = {},
        )
    }
}
