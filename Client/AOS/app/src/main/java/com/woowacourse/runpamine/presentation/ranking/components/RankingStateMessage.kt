package com.woowacourse.runpamine.presentation.ranking.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun RankingStateMessage(
    isLoading: Boolean,
    errorMessage: String?,
    isEmpty: Boolean,
    onRetryClick: () -> Unit,
) {
    when {
        isLoading -> {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        errorMessage != null -> {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = errorMessage,
                    color = Color(0xFF384152),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )
                Button(onClick = onRetryClick) {
                    Text(text = "다시 시도")
                }
            }
        }

        isEmpty -> {
            Text(
                text = "아직 랭킹에 표시할 기록이 없어요.",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp),
                color = Color(0xFF9AA3B2),
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RankingStateMessagePreview() {
    RunpamineTheme {
        RankingStateMessage(
            isLoading = false,
            errorMessage = "error",
            isEmpty = false,
            onRetryClick = {},
        )
    }
}
