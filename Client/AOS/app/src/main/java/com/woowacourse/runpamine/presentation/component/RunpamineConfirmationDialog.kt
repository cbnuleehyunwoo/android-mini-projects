package com.woowacourse.runpamine.presentation.component

import android.view.WindowManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

enum class ConfirmationDialogStyle {
    Default,
    Destructive,
}

@Composable
fun RunpamineConfirmationDialog(
    title: String,
    message: String,
    dismissText: String,
    confirmText: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    style: ConfirmationDialogStyle = ConfirmationDialogStyle.Default,
) {
    val isDestructive = style == ConfirmationDialogStyle.Destructive
    val confirmColor = if (isDestructive) Color(0xFFFF2D1A) else MaterialTheme.colorScheme.primary

    Dialog(onDismissRequest = onDismiss) {
        val dialogWindow = (LocalView.current.parent as DialogWindowProvider).window
        SideEffect {
            dialogWindow.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            dialogWindow.setDimAmount(DIALOG_DIM_AMOUNT)
        }

        Surface(
            modifier =
                modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (isDestructive) Color(0xFF111827) else MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isDestructive) Color(0xFF6B7280) else Color.Black,
                    textAlign = if (isDestructive) TextAlign.Center else TextAlign.Start,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (isDestructive) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF8A8A8A),
                                ),
                        ) {
                            Text(text = dismissText, color = Color.White)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            colors =
                                ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary,
                                ),
                        ) {
                            Text(text = dismissText)
                        }
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = confirmColor,
                            ),
                    ) {
                        Text(
                            text = confirmText,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

private const val DIALOG_DIM_AMOUNT = 0.45f

@Preview
@Composable
private fun RunpamineConfirmationDialogPreview() {
    RunpamineTheme {
        RunpamineConfirmationDialog(
            title = stringResource(R.string.update_required_title),
            message = stringResource(R.string.update_required_message),
            dismissText = stringResource(R.string.update_required_later),
            confirmText = stringResource(R.string.update_required_confirm),
            onDismiss = {},
            onConfirm = {},
        )
    }
}
