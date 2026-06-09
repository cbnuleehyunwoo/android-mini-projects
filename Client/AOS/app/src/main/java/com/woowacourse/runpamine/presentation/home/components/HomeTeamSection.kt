package com.woowacourse.runpamine.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun HomeTeamSection(
    teamName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(15.dp),
                ).padding(horizontal = 50.dp, vertical = 20.dp)
                .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Column {
            Text(
                text = teamName,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
            )
            Spacer(
                modifier =
                    Modifier
                        .height(8.dp),
            )
            ButtonWithIcon(
                onClick = onClick,
                text = stringResource(R.string.info_team),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 150)
@Composable
private fun HomeTeamSectionPreview() {
    RunpamineTheme {
        HomeTeamSection(
            teamName = "팀이름",
            onClick = {},
        )
    }
}
