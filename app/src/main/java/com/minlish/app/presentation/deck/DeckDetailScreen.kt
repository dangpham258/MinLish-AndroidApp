package com.minlish.app.presentation.deck

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.minlish.app.domain.model.Word
import com.minlish.app.presentation.common.component.BunnyAppBar
import com.minlish.app.presentation.common.component.BunnyBottomNavBar
import com.minlish.app.presentation.common.component.BunnyTab
import com.minlish.app.ui.theme.DeckColors
import com.minlish.app.ui.theme.DeckTypography

@Composable
fun DeckDetailScreen(
    viewModel: DeckViewModel,
    deckId: String,
    onNavigateBack: () -> Unit,
    onNavigateToAddWord: (String) -> Unit,
    onNavigateToUpdateWord: (String, String) -> Unit
) {
    LaunchedEffect(deckId) {
        viewModel.loadDeckDetails(deckId)
    }

    val deck by viewModel.currentDeck.collectAsState()
    val words by viewModel.words.collectAsState()

    Scaffold(
        containerColor = DeckColors.Background,
        topBar = {
            BunnyAppBar(title = "Bunny English", onBackClick = onNavigateBack)
        },
        bottomBar = {
            BunnyBottomNavBar(selectedTab = BunnyTab.LESSONS, onTabSelected = {})
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddWord(deckId) },
                containerColor = DeckColors.Primary,
                contentColor = DeckColors.SurfaceContainerLowest,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Word")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                deck?.let { d ->
                    // Header Card
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = DeckColors.PrimaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(DeckColors.SecondaryContainer, RoundedCornerShape(12.dp))
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Academic", style = DeckTypography.labelLg, color = DeckColors.OnSecondaryContainer)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = DeckColors.OnPrimaryContainer, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${d.totalWords} Words", style = DeckTypography.labelLg, color = DeckColors.OnPrimaryContainer)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(d.name, style = DeckTypography.headlineLg, color = DeckColors.OnPrimaryContainer)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Master common topics like Hobbies, Work, and Hometown with high-scoring vocabulary and natural idioms.",
                                style = DeckTypography.bodyMd,
                                color = DeckColors.OnPrimaryContainer
                            )
                        }
                    }
                }
            }

            item {
                deck?.let { d ->
                    // Progress Row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DeckColors.SurfaceContainerLowest),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp).background(DeckColors.SecondaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = DeckColors.Secondary)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Learned", style = DeckTypography.labelLg, color = DeckColors.OnSurface)
                                    Text("${d.learned}/${d.totalWords}", style = DeckTypography.headlineMd, color = DeckColors.Primary)
                                }
                            }
                        }
                        
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DeckColors.SurfaceContainerLowest),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(40.dp).background(DeckColors.TertiaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = DeckColors.TertiaryContainer)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Streak", style = DeckTypography.labelLg, color = DeckColors.OnSurface)
                                    Text("${d.streak} Days", style = DeckTypography.headlineMd, color = DeckColors.OnTertiaryContainer)
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Learning Modes
                Text("Learning Modes", style = DeckTypography.headlineMd, color = DeckColors.OnSurface)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LearningModeItem(
                        title = "Flashcard",
                        icon = Icons.Default.PlayArrow,
                        bgColor = DeckColors.PrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    LearningModeItem(
                        title = "Spaced Repetition",
                        icon = Icons.Default.Refresh,
                        bgColor = DeckColors.SecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    LearningModeItem(
                        title = "Context-based learning",
                        icon = Icons.Default.MenuBook,
                        bgColor = DeckColors.TertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                // Vocabulary List Title
                Text("Vocabulary List", style = DeckTypography.headlineMd, color = DeckColors.OnSurface)
            }

            items(words) { word ->
                WordCardItem(
                    word = word,
                    onClick = { onNavigateToUpdateWord(deckId, word.id) }
                )
            }
        }
    }
}

@Composable
fun LearningModeItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, bgColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier.aspectRatio(0.85f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(bgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = DeckColors.Primary)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = DeckTypography.labelMd, color = DeckColors.OnSurface, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun WordCardItem(word: Word, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DeckColors.SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(word.word, style = DeckTypography.titleLg, color = DeckColors.Primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(word.phonetic, style = DeckTypography.bodyMd, color = DeckColors.OnSurface)
                }
                Icon(Icons.Default.VolumeUp, contentDescription = "Play Audio", tint = DeckColors.PrimaryContainer)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(word.partOfSpeech, style = DeckTypography.labelLg, color = DeckColors.Outline, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            Spacer(modifier = Modifier.height(8.dp))
            Text(word.englishDefinition, style = DeckTypography.bodyMd, color = DeckColors.OnSurface)
        }
    }
}
