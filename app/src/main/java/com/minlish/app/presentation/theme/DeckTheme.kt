package com.minlish.app.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object DeckColors {
    val Background = Color(0xFFF0F9FF)
    val Primary = Color(0xFF0284C7)
    val PrimaryContainer = Color(0xFFE0F2FE)
    val OnPrimaryContainer = Color(0xFF0369A1)
    val OnPrimaryFixedVariant = Color(0xFF075985)
    val PrimaryFixedDim = Color(0xFF7DD3FC)
    val Secondary = Color(0xFF475569)
    val SecondaryContainer = Color(0xFFE0F2FE)
    val OnSecondaryContainer = Color(0xFF1E293B)
    val TertiaryContainer = Color(0xFFF8FAFC)
    val OnTertiaryContainer = Color(0xFF475569)
    val SurfaceContainerLowest = Color(0xFFFFFFFF)
    val SurfaceContainer = Color(0xFFE0F2FE)
    val SurfaceContainerHigh = Color(0xFFBAE6FD)
    val Outline = Color(0xFF64748B)
    val OutlineVariant = Color(0xFFCBD5E1)
    val OnSurface = Color(0xFF0F172A)
    val OnSurfaceVariant = Color(0xFF475569)
    val OnBackground = Color(0xFF0F172A)
    val Error = Color(0xFFEF4444)
    val ErrorContainer = Color(0xFFFEE2E2)
    val OnErrorContainer = Color(0xFF7F1D1D)
    val SurfaceVariant = Color(0xFFE2E8F0)
}

object DeckTypography {
    val headlineLg = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    )
    val headlineMd = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    )
    val titleLg = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    )
    val bodyLg = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp
    )
    val bodyMd = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
    val labelLg = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
    val labelMd = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
    val labelSm = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
}
