package com.minlish.app.presentation.common.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Định nghĩa 3 Tab chính chuẩn theo giao diện thiết kế
enum class BunnyTab(val title: String, val icon: ImageVector) {
    LESSONS("Lessons", Icons.Default.School),
    STATS("Stats", Icons.Default.Leaderboard),
    PROFILE("Profile", Icons.Default.Person)
}

@Composable
fun BunnyBottomNavBar(
    selectedTab: BunnyTab,
    onTabSelected: (BunnyTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp), // Bo góc phía trên của Footer
        shadowElevation = 8.dp, // Đổ bóng nhẹ hắt lên trên
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding() // Tránh bị đè bởi thanh điều hướng hệ thống của Android
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BunnyTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab

                // Hiệu ứng "viên thuốc" đổi màu nền khi click chuyển Tab
                Row(
                    modifier = Modifier
                        .animateContentSize() // Đột phá hiệu ứng tự phồng to mượt mà
                        .background(
                            color = if (isSelected) Color(0xFFA3D8FF) else Color.Transparent, // Màu nền xanh nhạt gốc
                            shape = RoundedCornerShape(999.dp) // Tròn xoe dạng viên thuốc
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null // Tắt hiệu ứng nhấp nháy mặc định để chạy mượt hơn
                        ) { onTabSelected(tab) }
                        .padding(horizontal = if (isSelected) 16.dp else 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = if (isSelected) Color(0xFF255F81) else Color(0xFF71787E) // Đổi màu icon khi chọn
                    )

                    if (isSelected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tab.title,
                            color = Color(0xFF255F81),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}