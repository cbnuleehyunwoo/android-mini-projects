package com.woowacourse.runpamine.presentation.createteam.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.presentation.component.BottomButton
import com.woowacourse.runpamine.presentation.component.ScreenTopBar
import com.woowacourse.runpamine.presentation.component.ValidatableTextField
import com.woowacourse.runpamine.ui.theme.Green40
import com.woowacourse.runpamine.ui.theme.Red40

@Composable
fun CreateTeamContent(
    teamName: String,
    isLengthValid: Boolean,
    hasAllowedCharacters: Boolean,
    hasNoSpecialCharacters: Boolean,
    onTeamNameChange: (String) -> Unit,
    validator: (String) -> Boolean,
    onCreateClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        ScreenTopBar(
            title = stringResource(R.string.create_team_bar),
            onBackClick = onBackClick,
        )
        Text(
            text = stringResource(R.string.create_team_header),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            lineHeight = 40.sp,
        )
        ValidatableTextField(
            value = teamName,
            onValueChange = onTeamNameChange,
            placeholder = stringResource(R.string.create_team_name_placeholder),
            validator = validator,
            modifier = Modifier.fillMaxWidth(),
        )
        ValidationConditions(
            isLengthValid = isLengthValid,
            hasAllowedCharacters = hasAllowedCharacters,
            hasNoSpecialCharacters = hasNoSpecialCharacters,
        )
        errorMessage?.let { message ->
            Text(
                text = message,
                color = Red40,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        BottomButton(
            text = stringResource(R.string.create_team),
            onClick = onCreateClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
        )
    }
}

@Composable
private fun ValidationConditions(
    isLengthValid: Boolean,
    hasAllowedCharacters: Boolean,
    hasNoSpecialCharacters: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ValidationConditionRow(
            isValid = isLengthValid,
            text = stringResource(R.string.create_team_condition_length),
        )
        ValidationConditionRow(
            isValid = hasAllowedCharacters,
            text = stringResource(R.string.create_team_condition_characters),
        )
        ValidationConditionRow(
            isValid = hasNoSpecialCharacters,
            text = stringResource(R.string.create_team_condition_special),
        )
    }
}

@Composable
private fun ValidationConditionRow(
    isValid: Boolean,
    text: String,
    modifier: Modifier = Modifier,
) {
    val color = if (isValid) Green40 else Red40
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = if (isValid) Icons.Default.Check else Icons.Default.Close,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}
