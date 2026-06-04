package com.woowacourse.runpamine.presentation.team.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun NoTeamIcon(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape,
                ).padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "팀",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(60.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoTeamIconPreview() {
    RunpamineTheme {
        NoTeamIcon()
    }
}
