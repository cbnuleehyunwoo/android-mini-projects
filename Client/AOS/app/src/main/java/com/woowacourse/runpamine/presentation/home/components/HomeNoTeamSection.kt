package com.woowacourse.runpamine.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
fun HomeNoTeamSection(
    onCreate: () -> Unit,
    onJoin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(15.dp),
                ).padding(horizontal = 50.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column {
            Text(
                text = stringResource(R.string.home_no_team_title),
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
            )
            Text(
                text = stringResource(R.string.home_no_team_description),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
            )
            Spacer(
                modifier =
                    Modifier
                        .height(8.dp),
            )
            ButtonSection(
                onCreate = onCreate,
                onJoin = onJoin,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ButtonSection(
    onCreate: () -> Unit,
    onJoin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ButtonWithIcon(
            text = stringResource(R.string.create_team),
            onClick = onCreate,
        )
        ButtonWithIcon(
            text = stringResource(R.string.join_team),
            onClick = onJoin,
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
private fun HomeNoTeamSectionPreview() {
    RunpamineTheme {
        HomeNoTeamSection(
            onJoin = {},
            onCreate = {},
        )
    }
}
