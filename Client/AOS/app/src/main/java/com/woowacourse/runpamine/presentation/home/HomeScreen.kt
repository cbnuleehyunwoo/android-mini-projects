package com.woowacourse.runpamine.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.presentation.home.components.HomeHeader
import com.woowacourse.runpamine.presentation.home.components.HomeMapSection
import com.woowacourse.runpamine.presentation.home.components.HomeNoTeamSection
import com.woowacourse.runpamine.presentation.home.components.StartButton
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun HomeScreen(
    name: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        HomeHeader(
            name = name,
        )
        HomeNoTeamSection(
            onCreate = {},
            onJoin = {},
            modifier = Modifier.padding(horizontal = 24.dp),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier.weight(1f),
        ) {
            HomeMapSection(modifier = Modifier.fillMaxSize())
            StartButton(
                onClick = {},
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    RunpamineTheme {
        HomeScreen(
            name = "호이",
        )
    }
}
