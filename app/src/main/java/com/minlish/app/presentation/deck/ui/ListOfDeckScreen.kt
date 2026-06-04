package com.minlish.app.presentation.deck.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import com.minlish.app.presentation.common.BunnyMainHeader
import com.minlish.app.presentation.deck.viewmodel.DeckListViewModel
import com.minlish.app.presentation.theme.DeckColors
import com.minlish.app.presentation.theme.DeckTypography

@Composable
fun ListOfDeckScreen(
    viewModel: DeckListViewModel,
    onNavigateToDeckDetail: (String) -> Unit,
    onNavigateToCreateDeck: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val decks by viewModel.decks.collectAsState()

    val filteredDecks by remember {
        derivedStateOf {
            if (searchQuery.isBlank()) decks
            else decks.filter { it.deckName.contains(searchQuery, ignoreCase = true) }
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
            title = { Text("Delete vocabulary list", style = DeckTypography.headlineMd) },
            text = {
                Text(
                    "Are you sure you want to delete the list \"${deckToDelete!!.name}\"? This action cannot be undone.",
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
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { deckToDelete = null }) {
                    Text("Cancel")
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
            title = { Text("Export CSV file", style = DeckTypography.headlineMd) },
            text = {
                Text(
                    "Do you want to include public vocabulary lists in the exported file?",
                    style = DeckTypography.bodyMd
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportDialog = false
                        viewModel.exportDecksToCSV(
                            decks = decks,
                            includePublic = true
                        ) { shareIntent ->
                            if (shareIntent != null) {
                                context.startActivity(Intent.createChooser(shareIntent, "Share CSV file"))
                                Toast.makeText(context, "Exported successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Export failed!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Yes, include everything")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showExportDialog = false
                        viewModel.exportDecksToCSV(
                            decks = decks,
                            includePublic = false
                        ) { shareIntent ->
                            if (shareIntent != null) {
                                context.startActivity(Intent.createChooser(shareIntent, "Share CSV file"))
                                Toast.makeText(context, "Exported successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Export failed!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("No, only mine")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = DeckColors.Background,
        topBar = {
            BunnyMainHeader(showNotification = false)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateDeck,
                containerColor = DeckColors.Primary,
                contentColor = DeckColors.SurfaceContainerLowest,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create new deck")
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
                text = "My vocabulary lists",
                style = DeckTypography.headlineLg,
                color = DeckColors.OnSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Manage and review your favorite topics.",
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
                },
                placeholder = { Text("Search vocabulary lists...", style = DeckTypography.bodyMd) },
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
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(items = filteredDecks, key = { deck -> deck.id }) { deck ->
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
                        Text(
                            text = "${deck.totalWords} word(s)",
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
                                contentDescription = "Delete vocabulary list",
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
                Text(text = "Progress: $progressPercent%", style = DeckTypography.labelLg, color = DeckColors.Outline)
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
            Text(text = "$progressPercent% complete", style = DeckTypography.labelLg, color = DeckColors.OnSurfaceVariant)
        }
    }
}
