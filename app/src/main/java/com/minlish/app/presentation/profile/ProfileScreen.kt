package com.minlish.app.presentation.profile

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.minlish.app.domain.model.enumration.InitialLevel
import java.util.Locale

// --- Cấu hình màu sắc ---
private val PrimaryColor = Color(0xFF4C635F)
private val SurfaceColor = Color(0xFFF9F9F9)
private val SecondaryColor = Color(0xFF50616B)
private val GoalSelectedBg = Color(0xFF4C635F)
private val GoalUnselectedBg = Color(0xFFD3E5F1).copy(alpha = 0.6f)

val ProfileAvatars = listOf(
    Icons.Default.Face, Icons.Default.Pets, Icons.Default.EmojiEmotions,
    Icons.Default.CatchingPokemon, Icons.Default.ChildCare, Icons.Default.Star
)

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    editViewModel: EditProfileViewModel = hiltViewModel(),
    notificationViewModel: NotificationViewModel = hiltViewModel(),
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 1. Kết nối States từ các ViewModel chuyên biệt
    val userData by viewModel.userData.collectAsState()
    val wordsLearned by viewModel.wordsLearned.collectAsState()
    val streak by viewModel.streak.collectAsState()

    val name by editViewModel.name.collectAsState()
    val email by editViewModel.emailAddress.collectAsState()
    val level by editViewModel.level.collectAsState()
    val levelTag by editViewModel.levelTag.collectAsState()
    val avatarIdx by editViewModel.avatarIndex.collectAsState()

    val emailNotify by notificationViewModel.emailNotification.collectAsState()
    val dailyReminder by notificationViewModel.dailyReminder.collectAsState()
    val spacedRepetition by notificationViewModel.spacedRepetition.collectAsState()
    val selectedGoals by notificationViewModel.selectedGoals.collectAsState()

    // Đồng bộ dữ liệu khi user data tải xong từ Firebase
    LaunchedEffect(userData) {
        userData?.let {
            editViewModel.loadProfile(it)
            notificationViewModel.loadSettings(it)
        }
    }

    var showEditDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(SurfaceColor)) {
        if (userData == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Header (Tên, Level, Avatar)
                ProfileHeader(name, level, levelTag, avatarIdx) { showEditDialog = true }

                // Stats (Bento Cards)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(String.format(Locale.getDefault(), "%,d", wordsLearned), "Words Learned", Icons.Outlined.AutoStories, Modifier.weight(1f))
                    StatCard(streak.toString(), "Streak", Icons.Filled.LocalFireDepartment, Modifier.weight(1f))
                }

                // Goals Section
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Learning Goals", fontWeight = FontWeight.Bold, color = Color(0xFF424847), fontSize = 13.sp)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        com.minlish.app.domain.model.enumration.LearningGoal.entries.forEach { goal ->
                            GoalChip(
                                text = goal.displayName,
                                isSelected = selectedGoals.contains(goal.name)
                            ) {
                                notificationViewModel.toggleGoal(goal.name)
                            }
                        }
                    }
                }

                // Notifications Group
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Notification Settings", fontWeight = FontWeight.Bold, color = Color(0xFF424847), fontSize = 13.sp)
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                        Column {
                            NotificationItem("Daily reminder", "Maintain study habits", Icons.Outlined.Alarm, Color(0xFFD0F0E8), PrimaryColor, dailyReminder) { notificationViewModel.setDailyReminder(it, email) }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F0F0))
                            NotificationItem("Spaced repetition", "Vocabulary review reminder", Icons.Outlined.Psychology, Color(0xFFD3E5F1), Color(0xFF50616B), spacedRepetition) { notificationViewModel.setSpacedRepetition(it) }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFF0F0F0))
                            NotificationItem("Notification Email", "Send a reminder to study", Icons.Outlined.Mail, Color(0xFFEEEEEE), Color(0xFF727877), emailNotify) { notificationViewModel.toggleEmailNotification(it, email) }
                        }
                    }
                }

                // Logout
                Row(modifier = Modifier.clickable {
                    viewModel.logout()
                    onLogout()
                }.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color(0xFFBA1A1A), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logout", color = Color(0xFFBA1A1A), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        if (showEditDialog) {
            EditProfileDialog(
                currentName = name,
                currentLevel = try { InitialLevel.valueOf(levelTag) } catch(e: Exception) { InitialLevel.B1 },
                currentAvatarIdx = avatarIdx,
                onDismiss = { showEditDialog = false },
                onSave = { n, l, idx ->
                    editViewModel.updateFullProfile(n, l, idx)
                    showEditDialog = false
                    Toast.makeText(context, "Updated Completed!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

// --- Component phụ ---
@Composable
fun EditProfileDialog(currentName: String, currentLevel: InitialLevel, currentAvatarIdx: Int, onDismiss: () -> Unit, onSave: (String, InitialLevel, Int) -> Unit) {
    var name by remember { mutableStateOf(currentName) }; var selectedLevel by remember { mutableStateOf(currentLevel) }; var idx by remember { mutableStateOf(currentAvatarIdx) }
    var showConfirm by remember { mutableStateOf(false) }
    if (showConfirm) {
        AlertDialog(onDismissRequest = { showConfirm = false }, title = { Text("Xác nhận") }, text = { Text("Lưu thay đổi?") }, confirmButton = { Button(onClick = { onSave(name, selectedLevel, idx); showConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)) { Text("Yes") } }, dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("No") } })
    }
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Edit Profile 🐰", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = PrimaryColor)
                Text("Choose Avatar:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    itemsIndexed(ProfileAvatars) { index, icon ->
                        Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(if (idx == index) Color(0xFFE6FFFA) else Color(0xFFF5F5F5)).border(2.dp, if (idx == index) PrimaryColor else Color.Transparent, CircleShape).clickable { idx = index }, contentAlignment = Alignment.Center) { Icon(icon, null, tint = PrimaryColor) }
                    }
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Họ tên") }, modifier = Modifier.fillMaxWidth())
                Text("Initial Level")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InitialLevel.entries.take(4).forEach { lvl ->
                        FilterChip(selected = selectedLevel == lvl, onClick = { selectedLevel = lvl }, label = { Text(lvl.name) })
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancle", color = SecondaryColor) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { showConfirm = true }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)) { Text("Save") }
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(name: String, level: String, levelTag: String, avatarIdx: Int, onEditClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(136.dp).clickable { onEditClick() }, contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(110.dp).clip(CircleShape).border(4.dp, Color(0xFFE6FFFA), CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                Icon(ProfileAvatars.getOrElse(avatarIdx) { Icons.Default.Person }, null, modifier = Modifier.size(56.dp), tint = PrimaryColor)
            }
            Surface(modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-8).dp, y = (-8).dp), shape = RoundedCornerShape(99.dp), color = Color(0xFF354B48), shadowElevation = 4.dp) {
                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Verified, null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Text(levelTag, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
        Text(level, color = SecondaryColor, fontSize = 16.sp)
    }
}

@Composable
fun StatCard(value: String, label: String, icon: ImageVector, modifier: Modifier) {
    Card(modifier = modifier.shadow(2.dp, RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(40.dp).background(Color(0xFFF0FDFA), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = PrimaryColor, modifier = Modifier.size(24.dp)) }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1C))
            Text(label, fontSize = 13.sp, color = SecondaryColor, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun GoalChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(99.dp),
        color = if (isSelected) PrimaryColor else GoalUnselectedBg,
        contentColor = if (isSelected) Color.White else Color(0xFF566771)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            if (isSelected) Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
fun NotificationItem(title: String, subtitle: String, icon: ImageVector, iconBg: Color, iconTint: Color, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(44.dp).background(iconBg, CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp)) }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1C1C))
            Text(subtitle, fontSize = 12.sp, color = SecondaryColor)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedTrackColor = PrimaryColor, uncheckedTrackColor = Color(0xFFE2E2E2), uncheckedThumbColor = Color.White))
    }
}
