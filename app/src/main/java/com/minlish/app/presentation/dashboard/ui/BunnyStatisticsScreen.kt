package com.minlish.app.presentation.dashboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minlish.app.presentation.common.BunnyMainHeader
import com.minlish.app.presentation.dashboard.viewmodel.StatisticsViewModel
import com.minlish.app.presentation.dashboard.ui.theme.*
import com.minlish.app.presentation.dashboard.ui.components.StatBoxElement
import com.minlish.app.presentation.dashboard.ui.components.BarItemNew
import com.minlish.app.presentation.dashboard.ui.components.RetentionCurveCard
import com.minlish.app.presentation.dashboard.ui.components.MilestoneCard
import java.util.Calendar

/**
 * BunnyStatisticsScreen — Màn hình thống kê.
 *
 * Nhận callbacks thay vì NavHostController để tuân thủ nguyên tắc
 * single-source-of-truth navigation (HomeScaffold điều hướng, không phải screen này).
 */
@Composable
fun BunnyStatisticsScreen(
    viewModel: StatisticsViewModel,
    modifier: Modifier = Modifier,
    onNavigateToSRS: (String) -> Unit = {}
) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.fetchDashboardTelemetry()
    }

    BunnyStatisticsContent(
        viewModel = viewModel,
        modifier = modifier,
        onNavigateToSRS = onNavigateToSRS
    )
}

@Composable
fun BunnyStatisticsContent(
    viewModel: StatisticsViewModel,
    modifier: Modifier = Modifier,
    onNavigateToSRS: (String) -> Unit = {}
) {
    val stats by viewModel.userProgress.collectAsState()
    val reports by viewModel.weeklyActivity.collectAsState()
    val setting by viewModel.userSetting.collectAsState()
    val latestNotification by viewModel.latestNotification.collectAsState()

    val todayName = remember {
        when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "T2"
            Calendar.TUESDAY -> "T3"
            Calendar.WEDNESDAY -> "T4"
            Calendar.THURSDAY -> "T5"
            Calendar.FRIDAY -> "T6"
            Calendar.SATURDAY -> "T7"
            Calendar.SUNDAY -> "CN"
            else -> ""
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Top Bar
        BunnyMainHeader(showNotification = false)

        // Nội dung chính bên trong Main
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 2. Level Estimation
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineVariant.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TRÌNH ĐỘ HIỆN TẠI",
                                fontSize = 14.sp,
                                color = Primary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = stats.currentLevelName,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = OnSurface
                            )
                        }
                        Text("🥕", fontSize = 36.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFECECFF))
                    ) {
                        val progressFraction = stats.levelProgress / 100f
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressFraction.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(PrimaryContainer, SecondaryContainer)
                                    ),
                                    shape = CircleShape
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tiến độ: ${stats.levelProgress}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceVariant
                        )
                        Text(
                            text = "Tiếp theo: ${stats.nextLevelName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            // 3. Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatBoxElement(
                    icon = Icons.Default.MenuBook,
                    value = stats.wordsCount.toString(),
                    label = "Từ Vựng",
                    containerColor = PrimaryFixed.copy(alpha = 0.3f),
                    contentColor = Primary,
                    modifier = Modifier.weight(1f)
                )
                StatBoxElement(
                    icon = Icons.Default.LocalFireDepartment,
                    value = stats.streak.toString(),
                    label = "Ngày Liên Tiếp",
                    containerColor = ErrorContainer.copy(alpha = 0.4f),
                    contentColor = ErrorColor,
                    modifier = Modifier.weight(1f)
                )
            }

            // 4. Daily Plan Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = TertiaryFixed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null,
                        tint = OnTertiaryFixed.copy(alpha = 0.08f),
                        modifier = Modifier
                            .size(140.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 16.dp, y = 16.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Kế Hoạch Hôm Nay",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OnTertiaryFixed,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 24.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.AddCircle, contentDescription = null, tint = OnTertiaryFixed, modifier = Modifier.size(20.dp))
                                Text(text = "Từ mới: ${setting.dailyNewWordGoal}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnTertiaryFixed)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Cached, contentDescription = null, tint = OnTertiaryFixed, modifier = Modifier.size(20.dp))
                                Text(text = "Ôn tập: ${setting.dailyReviewGoal}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = OnTertiaryFixed)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(Color(0xFFB3F0D4), RoundedCornerShape(16.dp))
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .align(Alignment.TopCenter)
                                    .background(SecondaryContainer, RoundedCornerShape(16.dp))
                                    .clickable { onNavigateToSRS("all_due") }
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "BẮT ĐẦU NGAY",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSecondaryContainer,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = OnSecondaryContainer)
                            }
                        }
                    }
                }
            }

            // 5. Biểu đồ cột: Hoạt Động Hàng Ngày
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, OutlineVariant.copy(alpha = 0.1f), RoundedCornerShape(24.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Hoạt Động Hàng Ngày", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = OnSurface)
                        Box(
                            modifier = Modifier
                                .background(PrimaryContainer.copy(alpha = 0.3f), CircleShape)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(text = "Phút", fontSize = 12.sp, color = Primary, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(128.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        val maxTime = reports.maxOfOrNull { it.timeMinutes } ?: 1
                        reports.forEach { report ->
                            BarItemNew(
                                report = report,
                                maxTime = maxTime,
                                isToday = report.dayName == todayName,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 6. Biểu đồ đường cong: Khả năng ghi nhớ (Retention Curve Card)
            RetentionCurveCard(stats = stats)

            // 7. Achievement Preview (Huy hiệu nhận thêm - Bản Premium)
            MilestoneCard(
                title = latestNotification?.title ?: "Daily reminder",
                content = latestNotification?.content ?: "Turn on reminders to keep your study habit steady.",
                label = if (latestNotification == null) "NOTIFICATION" else "LATEST NOTIFICATION",
                timeText = latestNotification?.createdAt?.let { formatNotificationTime(it) }.orEmpty()
            )
        }
    }
}

private fun formatNotificationTime(createdAt: Long): String {
    if (createdAt <= 0L) return ""
    val diffMs = (System.currentTimeMillis() - createdAt).coerceAtLeast(0L)
    val minuteMs = 60_000L
    val hourMs = 60 * minuteMs
    val dayMs = 24 * hourMs
    return when {
        diffMs < minuteMs -> "Just now"
        diffMs < hourMs -> "${diffMs / minuteMs} min ago"
        diffMs < dayMs -> "${diffMs / hourMs}h ago"
        else -> "${diffMs / dayMs}d ago"
    }
}
