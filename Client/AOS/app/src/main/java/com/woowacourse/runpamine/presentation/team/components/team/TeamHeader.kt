package com.woowacourse.runpamine.presentation.team.components.team

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.Blue40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun TeamHeader(
    teamName: String,
    onAddClick: () -> Unit,
    onLeaveTeamClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLeavingTeam: Boolean = false,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    var isLeaveDialogVisible by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = teamName,
            modifier = Modifier.weight(1f),
            style =
                MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 30.sp,
                    lineHeight = 36.sp,
                ),
            fontWeight = FontWeight.Bold,
            color = Blue40,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box {
            IconButton(onClick = { isMenuExpanded = true }) {
                Icon(
                    painter = painterResource(R.drawable.meatball),
                    contentDescription = stringResource(R.string.team_menu),
                    tint = Blue40,
                    modifier = Modifier.size(width = 28.dp, height = 7.dp),
                )
            }
            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false },
                modifier =
                    Modifier
                        .width(128.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFC8C6C2),
                            shape = MaterialTheme.shapes.extraLarge,
                        ),
                containerColor = Color.White,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                TeamMenuContent(
                    onInviteClick = {
                        isMenuExpanded = false
                        onAddClick()
                    },
                    onLeaveClick = {
                        isMenuExpanded = false
                        isLeaveDialogVisible = true
                    },
                )
            }
        }
    }

    if (isLeaveDialogVisible) {
        TeamLeaveDialog(
            onDismiss = { isLeaveDialogVisible = false },
            onLeaveClick = {
                isLeaveDialogVisible = false
                onLeaveTeamClick()
            },
            isLeavingTeam = isLeavingTeam,
        )
    }
}

@Composable
private fun TeamMenuContent(
    onInviteClick: () -> Unit,
    onLeaveClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.team_invite_member),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onInviteClick),
            color = Color.Black,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 10.dp),
            thickness = 1.dp,
            color = Color(0xFFC8C6C2),
        )
        Text(
            text = stringResource(R.string.team_leave),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLeaveClick),
            color = Color(0xFFE00000),
            fontSize = 18.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun TeamLeaveDialog(
    onDismiss: () -> Unit,
    onLeaveClick: () -> Unit,
    isLeavingTeam: Boolean,
) {
    Dialog(onDismissRequest = onDismiss) {
        val dialogWindow = (LocalView.current.parent as DialogWindowProvider).window
        SideEffect {
            dialogWindow.setDimAmount(0.2f)
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 36.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.team_leave),
                    color = Color(0xFF1F2937),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.team_leave_confirmation),
                    color = Color(0xFF6B7280),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(38.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    TeamLeaveDialogButton(
                        text = stringResource(R.string.cancel),
                        containerColor = Color(0xFF8E8E8C),
                        onClick = onDismiss,
                        enabled = !isLeavingTeam,
                        modifier = Modifier.weight(1f),
                    )
                    TeamLeaveDialogButton(
                        text = stringResource(R.string.team_leave),
                        containerColor = Blue40,
                        onClick = onLeaveClick,
                        enabled = !isLeavingTeam,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamLeaveDialogButton(
    text: String,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = Color.White,
            ),
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TeamHeaderPreview() {
    RunpamineTheme {
        TeamHeader(
            teamName = "볼트짱",
            onAddClick = {},
            onLeaveTeamClick = {},
        )
    }
}
