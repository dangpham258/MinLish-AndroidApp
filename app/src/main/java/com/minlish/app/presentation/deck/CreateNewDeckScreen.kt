package com.minlish.app.presentation.deck

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.minlish.app.domain.model.enumration.LearningGoal
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

    // Tags chỉ từ LearningGoal enum, không cho thêm tự do
    var selectedGoals by remember { mutableStateOf(setOf<LearningGoal>()) }

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
                        placeholder = {
                            Text(
                                "Ví dụ: IELTS Speaking Part 1",
                                style = DeckTypography.bodyMd,
                                color = DeckColors.Outline
                            )
                        },
                        trailingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = DeckColors.Outline)
                        },
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
                        placeholder = {
                            Text(
                                "Tổng hợp các từ vựng chủ đề giáo dục và công việc...",
                                style = DeckTypography.bodyMd,
                                color = DeckColors.Outline
                            )
                        },
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

            // Card 2: Tags từ LearningGoal enum
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DeckColors.SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Chọn mục tiêu học", style = DeckTypography.labelLg, color = DeckColors.OnSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Chọn một hoặc nhiều mục tiêu phù hợp với bộ từ của bạn",
                        style = DeckTypography.bodyMd,
                        color = DeckColors.OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LearningGoal.entries.forEach { goal ->
                            val isSelected = goal in selectedGoals
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedGoals = if (isSelected) {
                                        selectedGoals - goal
                                    } else {
                                        selectedGoals + goal
                                    }
                                },
                                label = {
                                    Text(goal.displayName, style = DeckTypography.labelLg)
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DeckColors.PrimaryContainer,
                                    selectedLabelColor = DeckColors.OnPrimaryContainer,
                                    selectedLeadingIconColor = DeckColors.OnPrimaryContainer,
                                    containerColor = DeckColors.SurfaceContainerHigh,
                                    labelColor = DeckColors.OnSurfaceVariant
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    selectedBorderColor = DeckColors.Primary,
                                    borderColor = Color.Transparent
                                )
                            )
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
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(DeckColors.SurfaceContainerLowest, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🐇", style = DeckTypography.headlineLg)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.createDeck(
                            name = name,
                            description = description,
                            // Truyền name của enum để ViewModel dùng LearningGoal.valueOf()
                            tags = selectedGoals.map { it.name }
                        )
                        onNavigateBack()
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeckColors.Primary,
                    disabledContainerColor = DeckColors.SurfaceContainerHigh
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Lưu bộ từ vựng", style = DeckTypography.titleLg)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
