package com.minlish.app.presentation.common

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
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.minlish.app.presentation.theme.DeckColors
import com.minlish.app.presentation.theme.DeckTypography
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
        color = DeckColors.SurfaceContainerLowest,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp), // Bo góc phía trên của Footer
        shadowElevation = 8.dp, // Đổ bóng nhẹ hắt lên trên
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding() // Tránh bị đè bởi thanh điều hướng hệ thống của Android
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BunnyTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onTabSelected(tab) }
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .background(
                            if (isSelected) DeckColors.PrimaryContainer else Color.Transparent,
                            RoundedCornerShape(24.dp)
                        )
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = if (isSelected) DeckColors.OnPrimaryContainer else DeckColors.Outline
                    )
                    Text(
                        text = tab.title,
                        style = DeckTypography.labelLg,
                        color = if (isSelected) DeckColors.OnPrimaryContainer else DeckColors.Outline
                    )
                }
            }
        }
    }
}