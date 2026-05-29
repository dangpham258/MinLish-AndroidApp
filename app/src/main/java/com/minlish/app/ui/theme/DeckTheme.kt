package com.minlish.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object DeckColors {
    val Background = Color(0xFFFBF8FF)
    val Primary = Color(0xFF2B6485)
    val PrimaryContainer = Color(0xFFA3D8FF)
    val OnPrimaryContainer = Color(0xFF255F81)
    val Secondary = Color(0xFF2F6953)
    val SecondaryContainer = Color(0xFFB3F0D4)
    val OnSecondaryContainer = Color(0xFF356F59)
    val TertiaryContainer = Color(0xFFE1D389)
    val OnTertiaryContainer = Color(0xFF645A1D)
    val SurfaceContainerLowest = Color(0xFFFFFFFF)
    val SurfaceContainerHigh = Color(0xFFE5E6FF)
    val Outline = Color(0xFF71787E)
    val OnSurface = Color(0xFF161A32)
    val OnSurfaceVariant = Color(0xFF41484D)
    val OnBackground = Color(0xFF161A32)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF93000A)
    val SurfaceVariant = Color(0xFFDEE0FF)
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
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
}
