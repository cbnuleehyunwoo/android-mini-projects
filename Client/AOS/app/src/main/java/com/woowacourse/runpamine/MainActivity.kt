package com.woowacourse.runpamine

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.woowacourse.runpamine.ui.theme.RunpamineTheme
import com.woowacourse.runpamine.ui.theme.RunpamineTypography

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.light(
                    scrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT,
                ),
        )
        setContent {
            RunpamineTheme {
                RunpamineApp()
            }
        }
    }
}

@Composable
fun Greeting(
    name: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "Hello $name!",
        modifier = modifier,
        style = RunpamineTypography.Header1,
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RunpamineTheme {
        Greeting("Android")
    }
}
