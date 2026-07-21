package com.woowacourse.runpamine.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.woowacourse.runpamine.R

val Pretendard =
    FontFamily(
        Font(R.font.pretendard_thin, FontWeight.Thin),
        Font(R.font.pretendard_extra_light, FontWeight.ExtraLight),
        Font(R.font.pretendard_light, FontWeight.Light),
        Font(R.font.pretendard_regular, FontWeight.Normal),
        Font(R.font.pretendard_medium, FontWeight.Medium),
        Font(R.font.pretendard_semi_bold, FontWeight.SemiBold),
        Font(R.font.pretendard_bold, FontWeight.Bold),
        Font(R.font.pretendard_extra_bold, FontWeight.ExtraBold),
        Font(R.font.pretendard_black, FontWeight.Black),
    )

val RunpamineBrand = FontFamily(Font(R.font.kyobo_handwriting_2025_lyb))

object RunpamineTypography {
    val Header1 =
        TextStyle(
            fontFamily = Pretendard,
            fontSize = 28.sp,
            lineHeight = 34.sp,
            letterSpacing = (-0.02).em,
            fontWeight = FontWeight.SemiBold,
        )

    val Header2 =
        TextStyle(
            fontFamily = Pretendard,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = (-0.02).em,
            fontWeight = FontWeight.SemiBold,
        )

    val Header3 =
        TextStyle(
            fontFamily = Pretendard,
            fontSize = 20.sp,
            lineHeight = 25.sp,
            letterSpacing = (-0.01).em,
            fontWeight = FontWeight.SemiBold,
        )

    val Header4 =
        TextStyle(
            fontFamily = Pretendard,
            fontSize = 20.sp,
            lineHeight = 25.sp,
            letterSpacing = (-0.01).em,
            fontWeight = FontWeight.SemiBold,
        )

    val Header5 =
        TextStyle(
            fontFamily = Pretendard,
            fontSize = 14.sp,
            lineHeight = 16.8.sp,
            letterSpacing = (-0.02).em,
            fontWeight = FontWeight.Bold,
        )

    val Body1 =
        TextStyle(
            fontFamily = Pretendard,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            letterSpacing = (-0.03).em,
            fontWeight = FontWeight.Medium,
        )

    val Body2 =
        TextStyle(
            fontFamily = Pretendard,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = (-0.03).em,
            fontWeight = FontWeight.Medium,
        )

    val Button =
        TextStyle(
            fontFamily = Pretendard,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            letterSpacing = (-0.02).em,
            fontWeight = FontWeight.Bold,
        )

    val NavigationTitle =
        TextStyle(
            fontFamily = Pretendard,
            fontSize = 19.sp,
            lineHeight = 24.sp,
            letterSpacing = (-0.02).em,
            fontWeight = FontWeight.Bold,
        )

    val TabLabel =
        TextStyle(
            fontFamily = Pretendard,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = (-0.02).em,
            fontWeight = FontWeight.Medium,
        )

    val Body3 =
        TextStyle(
            fontFamily = Pretendard,
            fontSize = 14.sp,
            lineHeight = 18.2.sp,
            letterSpacing = (-0.03).em,
            fontWeight = FontWeight.Medium,
        )

    val Caption1 =
        TextStyle(
            fontFamily = Pretendard,
            fontSize = 12.sp,
            lineHeight = 15.6.sp,
            letterSpacing = (-0.03).em,
            fontWeight = FontWeight.Normal,
        )

    val Caption2 =
        TextStyle(
            fontFamily = Pretendard,
            fontSize = 12.sp,
            lineHeight = 15.6.sp,
            letterSpacing = (-0.03).em,
            fontWeight = FontWeight.Normal,
        )

    val Caption3 =
        TextStyle(
            fontFamily = Pretendard,
            fontSize = 12.sp,
            lineHeight = 19.2.sp,
            letterSpacing = (-0.03).em,
            fontWeight = FontWeight.Medium,
        )

    val Caption4 =
        TextStyle(
            fontFamily = Pretendard,
            fontSize = 11.sp,
            lineHeight = 14.3.sp,
            letterSpacing = (-0.03).em,
            fontWeight = FontWeight.Medium,
        )
}

val AppTypography =
    Typography(
        displayLarge = RunpamineTypography.Header1,
        headlineLarge = RunpamineTypography.Header2,
        headlineMedium = RunpamineTypography.Header3,
        headlineSmall = RunpamineTypography.Header4,
        titleMedium = RunpamineTypography.Header5,
        bodyLarge = RunpamineTypography.Body1,
        bodyMedium = RunpamineTypography.Body2,
        bodySmall = RunpamineTypography.Body3,
        labelLarge = RunpamineTypography.Caption1,
        labelMedium = RunpamineTypography.Caption2,
        titleSmall = RunpamineTypography.Caption3,
        labelSmall = RunpamineTypography.Caption4,
    )
