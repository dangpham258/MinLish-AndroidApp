package com.minlish.app.presentation.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.minlish.app.R
import com.minlish.app.ui.theme.DeckColors
import com.minlish.app.ui.theme.DeckTypography

@Composable
fun BunnyLoadingScreen(
    message: String = "Đang chuẩn bị không gian học...",
    progress: Float = 0f
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeckColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.app_loading))
            val progressLottie by animateLottieCompositionAsState(
                composition = composition,
                iterations = LottieConstants.IterateForever
            )

            LottieAnimation(
                composition = composition,
                progress = { progressLottie },
                modifier = Modifier.size(240.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = message,
                style = DeckTypography.bodyLg,
                color = DeckColors.Primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "${(progress * 100).toInt()}%",
                style = DeckTypography.headlineLg.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = DeckColors.Secondary
            )
        }
    }
}

@Composable
fun RunningBunnyAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "bunny")
    
    val jumpOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jump"
    )

    Canvas(modifier = Modifier.size(100.dp)) {
        val center = Offset(size.width / 2, size.height / 2 + jumpOffset)
        
        // Vẽ thân thỏ (hình oval đơn giản)
        drawOval(
            color = DeckColors.PrimaryContainer,
            topLeft = Offset(center.x - 30.dp.toPx(), center.y - 20.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(60.dp.toPx(), 40.dp.toPx())
        )
        
        // Vẽ đầu thỏ
        drawCircle(
            color = DeckColors.PrimaryContainer,
            radius = 18.dp.toPx(),
            center = Offset(center.x + 25.dp.toPx(), center.y - 15.dp.toPx())
        )
        
        // Vẽ tai thỏ
        drawOval(
            color = DeckColors.PrimaryContainer,
            topLeft = Offset(center.x + 20.dp.toPx(), center.y - 45.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(10.dp.toPx(), 30.dp.toPx())
        )
        drawOval(
            color = DeckColors.PrimaryContainer,
            topLeft = Offset(center.x + 35.dp.toPx(), center.y - 40.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(10.dp.toPx(), 25.dp.toPx())
        )
        
        // Mắt
        drawCircle(
            color = Color.Black,
            radius = 2.dp.toPx(),
            center = Offset(center.x + 35.dp.toPx(), center.y - 18.dp.toPx())
        )
        
        // Chân thỏ đang chạy
        drawLine(
            color = DeckColors.Primary,
            start = Offset(center.x - 15.dp.toPx(), center.y + 15.dp.toPx()),
            end = Offset(center.x - 25.dp.toPx(), center.y + 25.dp.toPx()),
            strokeWidth = 4.dp.toPx()
        )
        drawLine(
            color = DeckColors.Primary,
            start = Offset(center.x + 10.dp.toPx(), center.y + 15.dp.toPx()),
            end = Offset(center.x + 5.dp.toPx(), center.y + 25.dp.toPx()),
            strokeWidth = 4.dp.toPx()
        )
    }
}
