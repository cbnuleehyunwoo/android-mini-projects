package com.woowacourse.runpamine.shared.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.shared.generated.resources.Res
import com.woowacourse.runpamine.shared.generated.resources.kyobo_handwriting_2025_lyb
import com.woowacourse.runpamine.shared.generated.resources.pretendard_black
import com.woowacourse.runpamine.shared.generated.resources.pretendard_bold
import com.woowacourse.runpamine.shared.generated.resources.pretendard_extra_bold
import com.woowacourse.runpamine.shared.generated.resources.pretendard_medium
import com.woowacourse.runpamine.shared.generated.resources.pretendard_regular
import com.woowacourse.runpamine.shared.generated.resources.pretendard_semi_bold
import org.jetbrains.compose.resources.Font

object RunpamineColors {
    val Primary = Color(0xFF0058FF)
    val TextPrimary = Color(0xFF121C2B)
    val TextSecondary = Color(0xFF6B788C)
    val Border = Color(0xFFDBE3ED)
    val Surface = Color(0xFFF7FAFC)
    val Success = Color(0xFF00C27A)
    val Danger = Color(0xFFFF3B30)
    val Kakao = Color(0xFFFFE000)
    val KakaoText = Color(0xFF291A00)
    val White = Color.White
    val Black = Color.Black
}

object RunpamineTypography {
    private lateinit var pretendard: FontFamily
    private lateinit var brand: FontFamily

    val Header1: TextStyle
        get() = TextStyle(fontFamily = pretendard, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
    val Header2: TextStyle
        get() = TextStyle(fontFamily = pretendard, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
    val Title2: TextStyle
        get() = TextStyle(fontFamily = pretendard, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
    val Body1: TextStyle
        get() = TextStyle(fontFamily = pretendard, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    val Body2: TextStyle
        get() = TextStyle(fontFamily = pretendard, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    val Caption1: TextStyle
        get() = TextStyle(fontFamily = pretendard, fontSize = 12.sp, fontWeight = FontWeight.Normal)
    val Button: TextStyle
        get() = TextStyle(fontFamily = pretendard, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    val SplashTitle: TextStyle
        get() = TextStyle(fontFamily = brand, fontSize = 48.sp, fontWeight = FontWeight.Normal)
    val LoginTitle: TextStyle
        get() = TextStyle(fontFamily = brand, fontSize = 44.sp, fontWeight = FontWeight.Normal)

    internal fun install(
        pretendard: FontFamily,
        brand: FontFamily,
    ) {
        this.pretendard = pretendard
        this.brand = brand
    }
}

@Composable
fun RunpamineTheme(content: @Composable () -> Unit) {
    val pretendard =
        FontFamily(
            Font(Res.font.pretendard_regular, FontWeight.Normal),
            Font(Res.font.pretendard_medium, FontWeight.Medium),
            Font(Res.font.pretendard_semi_bold, FontWeight.SemiBold),
            Font(Res.font.pretendard_bold, FontWeight.Bold),
            Font(Res.font.pretendard_extra_bold, FontWeight.ExtraBold),
            Font(Res.font.pretendard_black, FontWeight.Black),
        )
    val brand = FontFamily(Font(Res.font.kyobo_handwriting_2025_lyb, FontWeight.Normal))
    RunpamineTypography.install(pretendard = pretendard, brand = brand)

    val typography =
        Typography(
            displayLarge = RunpamineTypography.Header1,
            headlineLarge = RunpamineTypography.Header1,
            headlineMedium = RunpamineTypography.Header2,
            titleLarge = RunpamineTypography.Title2,
            bodyLarge = RunpamineTypography.Body1,
            bodyMedium = RunpamineTypography.Body2,
            labelSmall = RunpamineTypography.Caption1,
            labelLarge = RunpamineTypography.Button,
        )

    MaterialTheme(
        colorScheme =
            lightColorScheme(
                primary = RunpamineColors.Primary,
                onPrimary = Color.White,
                background = Color.White,
                onBackground = RunpamineColors.TextPrimary,
                surface = Color.White,
                onSurface = RunpamineColors.TextPrimary,
                error = RunpamineColors.Danger,
                outline = RunpamineColors.Border,
            ),
        typography = typography,
        content = content,
    )
}
