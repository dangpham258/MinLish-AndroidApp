package com.minlish.app.presentation.dashboard.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minlish.app.presentation.dashboard.model.UserProgress
import com.minlish.app.presentation.dashboard.ui.theme.SecondaryFixed
import com.minlish.app.presentation.dashboard.ui.theme.OnSecondaryContainer

@Composable
fun RetentionCurveCard(
    stats: UserProgress,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SecondaryFixed),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = OnSecondaryContainer)
                Text(text = "Khả Năng Ghi Nhớ", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSecondaryContainer)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vẽ Line Chart SVG mượt bằng Canvas dựa vào retentionRate từ Database (Tối đa 100%)
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
            ) {
                val width = size.width
                val height = size.height
                val retentionRateFactor = stats.retentionRate.coerceIn(0, 100) / 100f
                val endHeight = height * (1f - retentionRateFactor).coerceIn(0.05f, 0.95f)

                // Tạo path vẽ đường cong Bezier dựa trên tỷ lệ thực tế
                val strokePath = Path().apply {
                    moveTo(0f, height * 0.85f)
                    cubicTo(
                        width * 0.3f, height * 0.6f,
                        width * 0.6f, height * (1f - retentionRateFactor * 0.8f).coerceIn(0.05f, 0.95f),
                        width, endHeight
                    )
                }

                // Vẽ vùng fill mờ ở dưới
                val fillPath = Path().apply {
                    addPath(strokePath)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    color = OnSecondaryContainer.copy(alpha = 0.15f)
                )

                drawPath(
                    path = strokePath,
                    color = OnSecondaryContainer.copy(alpha = 0.85f),
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hộp thông tin tỷ lệ ghi nhớ động từ database
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Tỷ lệ ghi nhớ hiện tại", fontSize = 12.sp, color = OnSecondaryContainer.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
                    Text(text = "${stats.retentionRate}%", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = OnSecondaryContainer)
                }
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = null,
                    tint = OnSecondaryContainer,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
