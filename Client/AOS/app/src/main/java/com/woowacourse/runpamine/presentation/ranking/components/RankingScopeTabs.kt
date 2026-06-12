package com.woowacourse.runpamine.presentation.ranking.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.presentation.ranking.model.RankingScope
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun RankingScopeTabs(
    selectedScope: RankingScope,
    onScopeSelect: (RankingScope) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(72.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RankingScope.entries.forEach { scope ->
                RankingScopeTab(
                    scope = scope,
                    selected = scope == selectedScope,
                    onClick = { onScopeSelect(scope) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RankingScopePreview() {
    RunpamineTheme {
        RankingScopeTab(
            scope = RankingScope.PERSONAL,
            selected = true,
            onClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RankingScopeUnseletedPreview() {
    RunpamineTheme {
        RankingScopeTab(
            scope = RankingScope.PERSONAL,
            selected = false,
            onClick = {},
        )
    }
}
