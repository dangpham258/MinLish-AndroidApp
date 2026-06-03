package com.minlish.app.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object BunnyColors {
    val Primary = Color(0xFF0284C7)
    val PrimaryContainer = Color(0xFFE0F2FE)
    val Background = Color(0xFFF0F9FF)
    val Surface = Color(0xFFFFFFFF)
    val OnSurface = Color(0xFF0F172A)
    val Outline = Color(0xFF64748B)
    val OnSurfaceVariant = Color(0xFF475569)
    val OutlineVariant = Color(0xFFCBD5E1)
    val ErrorContainer = Color(0xFFFEE2E2)
    val OnErrorContainer = Color(0xFF7F1D1D)
    val Error = Color(0xFFEF4444)

    // Gradient cho Progress Bar
    val ProgressStart = Color(0xFF0284C7)
    val ProgressEnd = Color(0xFF7DD3FC)
}

// THÊM OBJECT NÀY VÀO FILE THEME CỦA BẠN
object ContextScreenColors {
    val Primary = Color(0xFF2B6485)
    val SkyBlue = Color(0xFFA3D8FF)
    val MintGreenHighlight = Color(0xFFB3F0D4)
    val OnSurfaceVariant = Color(0xFF41484D)
    val SurfaceBackground = Color(0xFFFBF8FF)
}

object BunnyTypography {
    val DisplayLg = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, color = BunnyColors.Primary)
    val HeadlineMd = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = BunnyColors.OnSurface)
    val BodyLg = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal, color = BunnyColors.OnSurface)
    val BodyMd = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal, color = BunnyColors.OnSurfaceVariant)
    val LabelMd = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.05.sp)
    val LabelSm = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
}

