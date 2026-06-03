package com.minlish.app.presentation.dashboard.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minlish.app.presentation.dashboard.model.ActivityReport
import com.minlish.app.presentation.dashboard.ui.theme.Primary
import com.minlish.app.presentation.dashboard.ui.theme.PrimaryFixed
import com.minlish.app.presentation.dashboard.ui.theme.OnSurfaceVariant

@Composable
fun BarItemNew(
    report: ActivityReport,
    maxTime: Int,
    isToday: Boolean,
    modifier: Modifier = Modifier
) {
    val heightFraction = if (maxTime > 0) report.timeMinutes.toFloat() / maxTime else 0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .fillMaxHeight(heightFraction.coerceIn(0.08f, 1.0f))
                    .background(
                        color = if (isToday) Primary else PrimaryFixed,
                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                    )
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = report.dayName,
            fontSize = 12.sp,
            color = if (isToday) Primary else OnSurfaceVariant,
            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "${report.timeMinutes} phút",
            fontSize = 10.sp,
            color = OnSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
