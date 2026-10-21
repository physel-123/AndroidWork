package com.example.jieqi.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 字体排版：正文使用系统中文字体，节气名称使用衬线（宋体/Noto Serif）以增强传统文化气质。
 */
val SerifFamily = FontFamily.Serif

private val Base = Typography()

val JieQiTypography = Base.copy(
    displayLarge = Base.displayLarge.copy(fontFamily = SerifFamily, fontWeight = FontWeight.Bold),
    displayMedium = Base.displayMedium.copy(fontFamily = SerifFamily, fontWeight = FontWeight.Bold),
    headlineLarge = Base.headlineLarge.copy(fontFamily = SerifFamily, fontWeight = FontWeight.SemiBold),
    headlineMedium = Base.headlineMedium.copy(fontFamily = SerifFamily, fontWeight = FontWeight.SemiBold),
    headlineSmall = Base.headlineSmall.copy(fontFamily = SerifFamily, fontWeight = FontWeight.SemiBold),
    titleLarge = Base.titleLarge.copy(fontFamily = SerifFamily, fontWeight = FontWeight.SemiBold),
    titleMedium = Base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    bodyLarge = Base.bodyLarge.copy(lineHeight = 26.sp),
    bodyMedium = Base.bodyMedium.copy(lineHeight = 23.sp),
    labelLarge = Base.labelLarge.copy(fontWeight = FontWeight.Medium)
)

/** 节气名称专用字号（首页大字标题）。 */
val TermTitleStyle: TextStyle = TextStyle(
    fontFamily = SerifFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 38.sp,
    lineHeight = 44.sp
)
