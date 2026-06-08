package com.woowacourse.runpamine.presentation.running.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.R
import com.woowacourse.runpamine.ui.theme.RunpamineTheme

@Composable
fun RunningControlButton(
    text: String,
    @DrawableRes iconResId: Int,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    borderColor: Color? = null,
) {
    Row(
        modifier =
            modifier
                .background(containerColor, RoundedCornerShape(10.dp))
                .then(
                    if (borderColor == null) {
                        Modifier
                    } else {
                        Modifier.border(1.dp, borderColor, RoundedCornerShape(10.dp))
                    },
                ).clickable(
                    role = Role.Button,
                    onClick = onClick,
                ).padding(horizontal = 18.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = text,
            modifier = Modifier.size(18.dp),
            colorFilter = ColorFilter.tint(contentColor),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 22.sp),
            fontWeight = FontWeight.Black,
            color = contentColor,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RunningControlButtonPreview() {
    RunpamineTheme {
        RunningControlButton(
            text = "일시정지",
            iconResId = R.drawable.ic_pause,
            containerColor = Color.Black,
            contentColor = Color.White,
            onClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
