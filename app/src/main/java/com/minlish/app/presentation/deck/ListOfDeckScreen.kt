package com.minlish.app.presentation.deck

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.minlish.app.domain.model.Deck
import com.minlish.app.presentation.common.component.BunnyAppBar
import com.minlish.app.presentation.common.component.BunnyBottomNavBar
import com.minlish.app.presentation.common.component.BunnyTab
import com.minlish.app.ui.theme.DeckColors
import com.minlish.app.ui.theme.DeckTypography

@Composable
fun ListOfDeckScreen(
    viewModel: DeckViewModel,
    onNavigateToDeckDetail: (String) -> Unit,
    onNavigateToCreateDeck: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val decks by viewModel.decks.collectAsState()

    Scaffold(
        containerColor = DeckColors.Background,
        topBar = {
            BunnyAppBar(title = "Bunny English", onBackClick = null)
        },
        bottomBar = {
            BunnyBottomNavBar(selectedTab = BunnyTab.LESSONS, onTabSelected = { })
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
                    onClick = { /* Import */ },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeckColors.SecondaryContainer,
                        contentColor = DeckColors.OnSecondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import", style = DeckTypography.labelLg)
                }

                Button(
                    onClick = { /* Export */ },
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

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(decks.filter { it.name.contains(searchQuery, ignoreCase = true) }) { deck ->
                    DeckCardItem(
                        deck = deck,
                        onClick = { onNavigateToDeckDetail(deck.id) }
                    )
                }
            }
        }
    }
}


@Composable
fun DeckCardItem(
    deck: Deck,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(DeckColors.PrimaryContainer.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = DeckColors.Primary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val progress = if (deck.totalWords > 0) (deck.learned.toFloat() / deck.totalWords.toFloat()) * 100 else 0f
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Tiến độ: ${progress.toInt()}%", style = DeckTypography.labelLg, color = DeckColors.Outline)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = DeckColors.Secondary,
                trackColor = DeckColors.SurfaceContainerHigh
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                deck.tags.forEachIndexed { index, tag ->
                    Box(
                        modifier = Modifier
                            .background(
                                if (index % 2 == 0) DeckColors.SecondaryContainer else DeckColors.PrimaryContainer,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tag,
                            style = DeckTypography.labelLg,
                            color = if (index % 2 == 0) DeckColors.OnSecondaryContainer else DeckColors.OnPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
