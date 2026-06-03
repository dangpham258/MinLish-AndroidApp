package com.minlish.app.presentation.deck

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.minlish.app.domain.model.Deck
import com.minlish.app.presentation.common.BunnyAppBar
import com.minlish.app.presentation.common.BunnyBottomNavBar
import com.minlish.app.presentation.common.BunnyTab
import com.minlish.app.presentation.theme.DeckColors
import com.minlish.app.presentation.theme.DeckTypography

@Composable
fun ListOfDeckScreen(
    viewModel: DeckViewModel,
    onNavigateToDeckDetail: (String) -> Unit,
    onNavigateToCreateDeck: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    // Thu thập danh sách Deck trực tiếp từ Flow kết nối với Firebase
    val decks by viewModel.decks.collectAsState()

    // Sử dụng derivedStateOf để tối ưu hóa việc lọc danh sách chủ đề theo tên,
    // tránh re-compute vô ích khi các trạng thái không liên quan thay đổi.
    val filteredDecks by remember {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                decks
            } else {
                decks.filter { it.deckName.contains(searchQuery, ignoreCase = true) }
            }
        }
    }

    // --- State cho dialog xóa deck ---
    var deckToDelete by remember { mutableStateOf<Deck?>(null) }

    // --- State cho dialog export ---
    var showExportDialog by remember { mutableStateOf(false) }

    // Dialog xác nhận xóa deck
    if (deckToDelete != null) {
        AlertDialog(
            onDismissRequest = { deckToDelete = null },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Xóa bộ từ vựng", style = DeckTypography.headlineMd) },
            text = {
                Text(
                    "Bạn có chắc muốn xóa bộ \"${deckToDelete!!.name}\"? Hành động này không thể hoàn tác.",
                    style = DeckTypography.bodyMd
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDeck(deckToDelete!!.id)
                        deckToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deckToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Dialog Export CSV: hỏi có bao gồm deck chung không
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            icon = {
                Icon(Icons.Default.Download, contentDescription = null, tint = DeckColors.Primary)
            },
            title = { Text("Xuất file CSV", style = DeckTypography.headlineMd) },
            text = {
                Text(
                    "Bạn có muốn bao gồm các bộ từ vựng chung (dùng chung) vào file xuất không?",
                    style = DeckTypography.bodyMd
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportDialog = false
                        viewModel.exportDecksToCSV(
                            decks = decks,
                            includePublic = true,
                            context = context
                        ) { uri ->
                            if (uri != null) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ file CSV"))
                                Toast.makeText(context, "Xuất file thành công!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Xuất file thất bại!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Có, bao gồm tất cả")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showExportDialog = false
                        viewModel.exportDecksToCSV(
                            decks = decks,
                            includePublic = false,
                            context = context
                        ) { uri ->
                            if (uri != null) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ file CSV"))
                                Toast.makeText(context, "Xuất file thành công!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Xuất file thất bại!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Không, chỉ của tôi")
                }
            }
        )
    }

    Scaffold(
        containerColor = DeckColors.Background,
        topBar = {
            BunnyAppBar(title = "Bunny English", onBackClick = null)
        },
        bottomBar = {
            BunnyBottomNavBar(
                selectedTab = BunnyTab.LESSONS,
                onTabSelected = { tab ->
                    when (tab) {
                        BunnyTab.LESSONS -> { /* already here */ }
                        BunnyTab.STATS -> onNavigateToDashboard()
                        BunnyTab.PROFILE -> onNavigateToProfile()
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateDeck,
                containerColor = DeckColors.Primary,
                contentColor = DeckColors.SurfaceContainerLowest,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create New Deck")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Bộ từ vựng của tôi",
                style = DeckTypography.headlineLg,
                color = DeckColors.OnSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Quản lý và ôn tập các chủ đề yêu thích của bạn.",
                style = DeckTypography.bodyMd,
                color = DeckColors.OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { showExportDialog = true },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeckColors.TertiaryContainer,
                        contentColor = DeckColors.OnTertiaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export", style = DeckTypography.labelLg)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchDecks(it)
                },
                placeholder = { Text("Tìm kiếm bộ từ vựng...", style = DeckTypography.bodyMd) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DeckColors.SurfaceContainerLowest,
                    unfocusedContainerColor = DeckColors.SurfaceContainerLowest,
                    focusedBorderColor = DeckColors.PrimaryContainer,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Hiển thị danh sách Deck từ Firebase sau khi lọc
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(
                    items = filteredDecks,
                    key = { deck -> deck.id } // Gán Key để tăng hiệu năng tái cấu trúc danh sách (Recomposition)
                ) { deck ->
                    val progressCount by viewModel.getDeckProgress(deck.id).collectAsState(initial = 0)
                    val totalCount = deck.vocabularyIds.size
                    val progressPercent = if (totalCount > 0) (progressCount.toFloat() / totalCount * 100).toInt() else 0

                    DeckCardItem(
                        deck = deck,
                        progressPercent = progressPercent,
                        onClick = { onNavigateToDeckDetail(deck.id) },
                        onDeleteClick = { deckToDelete = deck }
                    )
                }
            }
        }
    }
}

@Composable
fun DeckCardItem(
    deck: Deck,
    progressPercent: Int,
    onClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeckColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deck.name,
                        style = DeckTypography.headlineMd,
                        color = DeckColors.OnBackground,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = DeckColors.OnSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${deck.totalWords} từ",
                            style = DeckTypography.labelLg,
                            color = DeckColors.OnSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Nút xóa — chỉ hiện với deck do người dùng tạo (isPublic=false)
                    if (!deck.isPublic && onDeleteClick != null) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Xóa bộ từ vựng",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(DeckColors.PrimaryContainer.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = DeckColors.Primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Tiến độ: $progressPercent%", style = DeckTypography.labelLg, color = DeckColors.Outline)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progressPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = DeckColors.Secondary,
                trackColor = DeckColors.SurfaceContainerHigh
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hiển thị danh sách nhãn mục tiêu (Tags) được lấy trực tiếp từ cấu trúc Firebase
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                deck.tags.take(3).forEachIndexed { index, tag ->
                    Box(
                        modifier = Modifier
                            .background(
                                if (index % 2 == 0) DeckColors.SecondaryContainer else DeckColors.PrimaryContainer,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tag.name,
                            style = DeckTypography.labelLg,
                            color = if (index % 2 == 0) DeckColors.OnSecondaryContainer else DeckColors.OnPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
