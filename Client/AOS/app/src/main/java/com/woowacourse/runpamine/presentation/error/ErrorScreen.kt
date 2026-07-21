package com.woowacourse.runpamine.presentation.error

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.Gray40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun ErrorScreen(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitPointerEvent().changes.forEach { change -> change.consume() }
                    }
                },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.img_error),
            contentDescription = null,
            modifier = Modifier.size(300.dp),
        )
        Spacer(modifier = Modifier.height(15.dp))
        Text(
            text = stringResource(R.string.network_error_message),
            style = MaterialTheme.typography.bodyLarge,
            color = Gray40,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ErrorScreenPreview() {
    RunpamineTheme {
        ErrorScreen()
    }
}
