package com.woowacourse.runpamine.presentation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woowacourse.runpamine.ui.theme.Red40
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun ValidatableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    validator: (String) -> Boolean = { true },
) {
    val isError = value.isNotEmpty() && !validator(value)

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = placeholder) },
            isError = isError,
            singleLine = singleLine,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                errorBorderColor = Red40,
                cursorColor = MaterialTheme.colorScheme.primary,
                errorCursorColor = Red40,
            ),
        )
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
private fun ValidatableTextFieldValidPreview() {
    RunpamineTheme {
        ValidatableTextField(
            value = "러닝 크루",
            onValueChange = {},
            placeholder = "팀 이름을 입력하세요",
            validator = { it.length <= 10 },
        )
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
private fun ValidatableTextFieldErrorPreview() {
    RunpamineTheme {
        ValidatableTextField(
            value = "너무너무너무긴팀이름입니다",
            onValueChange = {},
            placeholder = "팀 이름을 입력하세요",
            validator = { it.length <= 10 },
        )
    }
}
