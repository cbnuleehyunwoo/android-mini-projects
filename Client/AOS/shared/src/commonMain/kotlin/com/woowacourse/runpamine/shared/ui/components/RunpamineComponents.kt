package com.woowacourse.runpamine.shared.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.woowacourse.runpamine.shared.generated.resources.Res
import com.woowacourse.runpamine.shared.generated.resources.icon_back
import com.woowacourse.runpamine.shared.generated.resources.icon_history
import com.woowacourse.runpamine.shared.generated.resources.icon_home
import com.woowacourse.runpamine.shared.generated.resources.icon_rank
import com.woowacourse.runpamine.shared.generated.resources.icon_team
import com.woowacourse.runpamine.shared.ui.model.MainTab
import com.woowacourse.runpamine.shared.ui.theme.RunpamineColors
import com.woowacourse.runpamine.shared.ui.theme.RunpamineTypography
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun PrimaryButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    containerColor: Color = RunpamineColors.Primary,
    contentColor: Color = Color.White,
) {
    val canClick = enabled && !isLoading
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (canClick) containerColor else containerColor.copy(alpha = 0.45f))
                .clickable(enabled = canClick, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = contentColor, strokeWidth = 2.5.dp)
        } else {
            Text(text = title, style = RunpamineTypography.Button, color = contentColor)
        }
    }
}

@Composable
fun TopNavigationBar(
    title: String,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    closeStyle: Boolean = false,
) {
    Box(modifier = modifier.fillMaxWidth().height(60.dp)) {
        if (onBack != null) {
            Box(
                modifier = Modifier.align(Alignment.CenterStart).size(44.dp).clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                if (closeStyle) {
                    Text("×", color = RunpamineColors.TextPrimary, fontSize = 30.sp, fontWeight = FontWeight.Light)
                } else {
                    Image(
                        painter = painterResource(Res.drawable.icon_back),
                        contentDescription = "뒤로가기",
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
        Text(
            text = title,
            style = RunpamineTypography.Body1.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold),
            color = RunpamineColors.TextPrimary,
            modifier = Modifier.align(Alignment.Center),
        )
        Spacer(modifier = Modifier.align(Alignment.CenterEnd).size(44.dp))
    }
}

@Composable
fun AppTabBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().background(Color.White)) {
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(RunpamineColors.Border))
        Row(
            modifier = Modifier.fillMaxWidth().height(74.dp).padding(horizontal = 30.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            MainTab.entries.forEach { tab ->
                val selected = selectedTab == tab
                Column(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onTabSelected(tab) },
                            ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        painter = painterResource(tab.iconResource()),
                        contentDescription = tab.label,
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit,
                        alpha = if (selected) 1f else 0.42f,
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = tab.label,
                        style = RunpamineTypography.Body1.copy(fontWeight = FontWeight.Medium),
                        color = if (selected) RunpamineColors.Primary else RunpamineColors.TextSecondary,
                    )
                }
            }
        }
    }
}

private fun MainTab.iconResource(): DrawableResource =
    when (this) {
        MainTab.Home -> Res.drawable.icon_home
        MainTab.Team -> Res.drawable.icon_team
        MainTab.Ranking -> Res.drawable.icon_rank
        MainTab.History -> Res.drawable.icon_history
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
    isDanger: Boolean = false,
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.38f)).padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(21.dp))
                        .background(Color.White)
                        .padding(horizontal = 24.dp, vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    style = RunpamineTypography.Header2.copy(fontWeight = FontWeight.Bold),
                    color = RunpamineColors.TextPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    text = message,
                    style = RunpamineTypography.Body1,
                    color = RunpamineColors.TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                )
                Spacer(Modifier.height(28.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    DialogButton(dismissText, onDismiss, Modifier.weight(1f), false)
                    DialogButton(confirmText, onConfirm, Modifier.weight(1f), isDanger)
                }
            }
        }
    }
}

@Composable
private fun DialogButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier,
    isDanger: Boolean,
) {
    Box(
        modifier =
            modifier
                .height(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDanger) RunpamineColors.Danger else RunpamineColors.Primary)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(title, style = RunpamineTypography.Button, color = Color.White)
    }
}

@Composable
fun ValidationRuleRow(
    text: String,
    isValid: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(
            text = if (isValid) "✓" else "×",
            color = if (isValid) RunpamineColors.Success else RunpamineColors.Danger,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
        Text(
            text = text,
            style = RunpamineTypography.Body1,
            color = if (isValid) RunpamineColors.Success else RunpamineColors.TextSecondary,
        )
    }
}

@Composable
fun CheckBox(
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Int = 28,
) {
    Box(
        modifier =
            modifier
                .size(size.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(if (checked) RunpamineColors.Primary else RunpamineColors.Border)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Canvas(modifier = Modifier.size((size * 0.58f).dp)) {
                val path =
                    Path().apply {
                        moveTo(this@Canvas.size.width * 0.08f, this@Canvas.size.height * 0.52f)
                        lineTo(this@Canvas.size.width * 0.40f, this@Canvas.size.height * 0.82f)
                        lineTo(this@Canvas.size.width * 0.92f, this@Canvas.size.height * 0.16f)
                    }
                drawPath(path, color = Color.White, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round))
            }
        }
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = RunpamineColors.TextPrimary,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(RunpamineColors.Surface)
                .border(1.dp, RunpamineColors.Border, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(label, style = RunpamineTypography.Body2, color = RunpamineColors.TextSecondary)
        Spacer(Modifier.height(9.dp))
        Text(value, style = RunpamineTypography.Title2.copy(fontWeight = FontWeight.Bold), color = valueColor)
    }
}
