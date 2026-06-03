package com.minlish.app.presentation.deck

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.minlish.app.presentation.common.BunnyAppBar
import com.minlish.app.presentation.theme.DeckColors
import com.minlish.app.presentation.theme.DeckTypography

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateNewDeckScreen(
    viewModel: DeckViewModel,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    val recommendedTags = listOf("Daily", "Travel", "Work", "Fun")
    var selectedTags by remember { mutableStateOf(setOf("IELTS", "Business")) }

    Scaffold(
        containerColor = DeckColors.Background,
        topBar = {
            BunnyAppBar(title = "Tạo bộ từ vựng mới", onBackClick = onNavigateBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Input Fields
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DeckColors.SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tên bộ từ", style = DeckTypography.labelLg, color = DeckColors.OnSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Ví dụ: IELTS Speaking Part 1", style = DeckTypography.bodyMd, color = DeckColors.Outline) },
                        trailingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = DeckColors.Outline) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeckColors.PrimaryContainer,
                            unfocusedBorderColor = DeckColors.PrimaryContainer,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Mô tả", style = DeckTypography.labelLg, color = DeckColors.OnSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = { Text("Tổng hợp các từ vựng chủ đề giáo dục và công việc...", style = DeckTypography.bodyMd, color = DeckColors.Outline) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeckColors.PrimaryContainer,
                            unfocusedBorderColor = DeckColors.PrimaryContainer,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
            }
            
            // Card 2: Tags
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DeckColors.SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Thêm/Chọn Tags", style = DeckTypography.labelLg, color = DeckColors.OnSurface)
                        Text("TÙY CHỌN", style = DeckTypography.labelLg, color = DeckColors.PrimaryContainer)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedTags.forEachIndexed { index, tag ->
                            Row(
                                modifier = Modifier
                                    .background(
                                        if (index % 2 == 0) DeckColors.TertiaryContainer else DeckColors.SecondaryContainer,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tag,
                                    style = DeckTypography.labelLg,
                                    color = if (index % 2 == 0) DeckColors.OnTertiaryContainer else DeckColors.OnSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier.size(16.dp).clickable { selectedTags = selectedTags - tag },
                                    tint = if (index % 2 == 0) DeckColors.OnTertiaryContainer else DeckColors.OnSecondaryContainer
                                )
                            }
                        }
                        
                        // Add Tag Button
                        Row(
                            modifier = Modifier
                                .border(1.dp, DeckColors.PrimaryContainer, RoundedCornerShape(16.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = DeckColors.Primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Thêm mới", style = DeckTypography.labelLg, color = DeckColors.Primary)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Gợi ý cho bạn:", style = DeckTypography.labelMd, color = DeckColors.Outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recommendedTags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .background(DeckColors.SurfaceContainerHigh, RoundedCornerShape(8.dp))
                                    .clickable { selectedTags = selectedTags + tag }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(tag, style = DeckTypography.labelLg, color = DeckColors.Outline)
                            }
                        }
                    }
                }
            }
            
            // Card 3: Banner
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DeckColors.SurfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Học tập hiệu quả hơn!", style = DeckTypography.headlineMd, color = DeckColors.OnPrimaryContainer)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Càng nhiều từ vựng, khả năng giao tiếp của bạn càng bay cao.",
                            style = DeckTypography.bodyMd,
                            color = DeckColors.Primary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    // Placeholder for Bunny Mascot
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(DeckColors.SurfaceContainerLowest, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Bunny", style = DeckTypography.labelLg, color = DeckColors.Primary)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    viewModel.createDeck(name, description, selectedTags.toList())
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeckColors.Primary)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lưu bộ từ vựng", style = DeckTypography.titleLg)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
