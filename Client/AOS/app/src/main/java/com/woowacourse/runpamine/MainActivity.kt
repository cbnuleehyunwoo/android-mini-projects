package com.woowacourse.runpamine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.woowacourse.runpamine.presentation.createteam.CreateTeamScreen
import com.woowacourse.runpamine.presentation.home.HomeScreen
import com.woowacourse.runpamine.presentation.home.components.HomeMapSection
import com.woowacourse.runpamine.ui.theme.RunpamineTypography
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RunpamineTheme {
                CreateTeamScreen()
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
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
