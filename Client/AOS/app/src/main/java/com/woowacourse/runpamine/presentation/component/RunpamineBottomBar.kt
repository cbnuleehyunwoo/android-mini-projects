package com.woowacourse.runpamine.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.presentation.navigation.AppRoute
import com.woowacourse.runpamine.ui.theme.Blue40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

enum class RunpamineBottomTab(
    val label: String,
    val iconResId: Int,
    val route: String,
) {
    Home(label = "홈", iconResId = R.drawable.ic_home, route = AppRoute.Home.route),
    Team(label = "팀", iconResId = R.drawable.ic_team, route = AppRoute.Team.route),
    Ranking(label = "랭킹", iconResId = R.drawable.mdi_prize, route = AppRoute.Ranking.route),
    History(label = "기록", iconResId = R.drawable.ic_record, route = AppRoute.Record.route),
}

@Composable
fun RunpamineBottomBar(
    selectedTab: RunpamineBottomTab,
    onTabClick: (RunpamineBottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = Color(0xFFE5E7EB),
        )
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(75.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RunpamineBottomTab.entries.forEach { tab ->
                RunpamineBottomBarItem(
                    tab = tab,
                    selected = selectedTab == tab,
                    onClick = { onTabClick(tab) },
                )
            }
        }
    }
}

@Composable
private fun RunpamineBottomBarItem(
    tab: RunpamineBottomTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = if (selected) Blue40 else Color(0xFF8B8B8B)

    Column(
        modifier =
            modifier
                .size(width = 72.dp, height = 64.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    role = Role.Tab,
                    onClick = onClick,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BottomTabIcon(
            tab = tab,
            tint = color,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = tab.label,
            fontSize = 12.sp,
            color = color,
        )
    }
}

@Composable
private fun BottomTabIcon(
    tab: RunpamineBottomTab,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painterResource(id = tab.iconResId),
        contentDescription = tab.label,
        modifier = modifier,
        tint = tint,
    )
}

@Preview(showBackground = true, heightDp = 100)
@Composable
private fun RunpamineBottomBarPreview() {
    RunpamineTheme {
        RunpamineBottomBar(
            selectedTab = RunpamineBottomTab.Team,
            onTabClick = {},
        )
    }
}
