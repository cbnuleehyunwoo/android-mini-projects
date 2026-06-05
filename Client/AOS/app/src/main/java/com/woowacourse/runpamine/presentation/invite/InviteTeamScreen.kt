package com.woowacourse.runpamine.presentation.invite

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.presentation.component.ScreenTopBar
import com.woowacourse.runpamine.presentation.invite.components.CopyCodeButton
import com.woowacourse.runpamine.presentation.invite.components.InviteCode
import com.woowacourse.runpamine.presentation.invite.components.InviteTeamHeader
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun InviteTeamScreen(
    code: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = modifier,
    ) {
        ScreenTopBar(
            title = stringResource(R.string.invite_team_bar),
            onBackClick = onBackClick,
        )
        Spacer(
            modifier = Modifier.height(15.dp),
        )
        InviteTeamHeader(
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(
            modifier = Modifier.height(24.dp),
        )
        InviteCode(
            code = code,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Spacer(
            modifier = Modifier.height(16.dp),
        )
        CopyCodeButton(
            onClick = {
                clipboardManager.setText(AnnotatedString(code))
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@Preview(showBackground = true, widthDp = 500)
@Composable
private fun InviteTeamScreenPreview() {
    RunpamineTheme {
        InviteTeamScreen(
            code = "ADOM34",
            onBackClick = {},
        )
    }
}
