package com.woowacourse.runpamine.presentation.ranking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.presentation.ranking.model.RankingScope
import com.woowacourse.runpamine.ui.theme.Blue40
import com.woowacourse.runpamine.ui.theme.Gray40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun RankingScopeTab(
    scope: RankingScope,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .height(72.dp)
                .clickable(
                    role = Role.Tab,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = scope.label,
            color = if (selected) Blue40 else Gray40,
            style =
                MaterialTheme.typography.titleSmall.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                ),
            textAlign = TextAlign.Center,
        )
        if (selected) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(Blue40),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RankingScopeTabPreview() {
    RunpamineTheme {
        RankingScopeTab(
            scope = RankingScope.PERSONAL,
            selected = false,
            onClick = {},
        )
    }
}
