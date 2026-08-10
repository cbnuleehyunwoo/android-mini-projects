package com.woowacourse.runpamine.presentation.feedback

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.presentation.component.BottomButton
import com.woowacourse.runpamine.presentation.component.ScreenTopBar
import com.woowacourse.runpamine.ui.theme.RunpamineLayout
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

private enum class FeedbackOption(
    @StringRes val labelResId: Int,
) {
    CHARACTER_CUSTOMIZING(R.string.feedback_option_character),
    EARN_POINTS(R.string.feedback_option_points),
    NOTIFY_INCOMPLETE_MEMBERS(R.string.feedback_option_notify),
    INDOOR_RUNNING(R.string.feedback_option_indoor),
    PROOF_PHOTO(R.string.feedback_option_photo),
    CUSTOM(R.string.feedback_option_custom),
}

private val textPrimaryColor = Color(0xFF111827)
private val textSecondaryColor = Color(0xFF7B7F87)
private val borderColor = Color(0xFFD6DAE1)

@Composable
fun FeedbackScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isSubmitted by remember { mutableStateOf(false) }

    if (isSubmitted) {
        FeedbackCompleteContent(onConfirm = onBackClick, modifier = modifier)
    } else {
        FeedbackWritingContent(
            onBackClick = onBackClick,
            onSubmit = { isSubmitted = true },
            modifier = modifier,
        )
    }
}

@Composable
private fun FeedbackWritingContent(
    onBackClick: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedOptions = remember { mutableStateOf(emptySet<FeedbackOption>()) }
    var customText by remember { mutableStateOf("") }
    var inconvenienceText by remember { mutableStateOf("") }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White),
    ) {
        ScreenTopBar(
            title = stringResource(R.string.feedback_title),
            onBackClick = onBackClick,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = RunpamineLayout.NavigationTopPadding),
        )

        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = RunpamineLayout.ScreenHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.feedback_question),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimaryColor,
            )

            FeedbackOption.entries.forEach { option ->
                FeedbackOptionRow(
                    label = stringResource(option.labelResId),
                    isSelected = option in selectedOptions.value,
                    onClick = {
                        selectedOptions.value =
                            if (option in selectedOptions.value) {
                                selectedOptions.value - option
                            } else {
                                selectedOptions.value + option
                            }
                    },
                )
            }

            if (FeedbackOption.CUSTOM in selectedOptions.value) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.feedback_custom_label),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor,
                )
                FeedbackTextField(
                    value = customText,
                    onValueChange = { customText = it },
                    placeholder = stringResource(R.string.feedback_custom_placeholder),
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.feedback_inconvenience_question),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimaryColor,
            )
            FeedbackTextField(
                value = inconvenienceText,
                onValueChange = { inconvenienceText = it },
                placeholder = stringResource(R.string.feedback_inconvenience_placeholder),
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        BottomButton(
            text = stringResource(R.string.feedback_submit),
            onClick = onSubmit,
            enabled = selectedOptions.value.isNotEmpty(),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = RunpamineLayout.ScreenHorizontalPadding)
                    .padding(bottom = 14.dp),
        )
    }
}

@Composable
private fun FeedbackOptionRow(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) primaryColor.copy(alpha = 0.06f) else Color.White)
                .border(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) primaryColor else borderColor,
                    shape = RoundedCornerShape(10.dp),
                ).clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) primaryColor else Color.White)
                    .border(
                        width = 1.5.dp,
                        color = if (isSelected) primaryColor else borderColor,
                        shape = RoundedCornerShape(6.dp),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) primaryColor else textPrimaryColor,
        )
    }
}

@Composable
private fun FeedbackTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier =
            modifier
                .fillMaxWidth()
                .height(140.dp),
        placeholder = {
            Text(
                text = placeholder,
                color = textSecondaryColor,
            )
        },
        keyboardOptions = KeyboardOptions.Default,
        shape = RoundedCornerShape(8.dp),
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFF5F6F8),
                unfocusedContainerColor = Color(0xFFF5F6F8),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
    )
}

@Composable
private fun FeedbackCompleteContent(
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = RunpamineLayout.ScreenHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(R.drawable.img_feedback_complete),
            contentDescription = null,
            modifier = Modifier.size(240.dp),
        )
        Text(
            text = stringResource(R.string.feedback_complete_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimaryColor,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = stringResource(R.string.feedback_complete_message),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = textSecondaryColor,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(top = 16.dp),
        )

        Spacer(modifier = Modifier.weight(1f))

        BottomButton(
            text = stringResource(R.string.feedback_confirm),
            onClick = onConfirm,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun FeedbackScreenPreview() {
    RunpamineTheme {
        FeedbackScreen(onBackClick = {})
    }
}
