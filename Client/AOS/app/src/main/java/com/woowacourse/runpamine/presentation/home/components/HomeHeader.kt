package com.woowacourse.runpamine.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun HomeHeader(
    name: String,
    onMyPageClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        DefaultProfileImage()
        GreetMessage(
            name = name,
            modifier = Modifier.weight(1f),
        )
        Icon(
            painter = painterResource(R.drawable.person_outline),
            contentDescription = "마이페이지 이동",
            modifier =
                Modifier
                    .size(24.dp)
                    .clickable { onMyPageClick() },
        )
    }
}

@Composable
private fun GreetMessage(
    name: String,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier,
    ) {
        Text(
            text = stringResource(R.string.home_greeting, name),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = stringResource(R.string.home_greeting_weather),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun DefaultProfileImage(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(15.dp))
                .background(MaterialTheme.colorScheme.primary),
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "프로필 사진",
            tint = Color.White,
            modifier = Modifier.size(40.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun HomeHeaderPreview() {
    RunpamineTheme {
        HomeHeader(
            name = "호이",
            onMyPageClick = {},
        )
    }
}
