package com.minlish.app.presentation.deck

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.minlish.app.domain.model.Word
import com.minlish.app.presentation.common.component.BunnyAppBar
import com.minlish.app.ui.theme.DeckColors
import com.minlish.app.ui.theme.DeckTypography

@Composable
fun AddUpdateWordScreen(
    viewModel: DeckViewModel,
    deckId: String,
    wordId: String?,
    onNavigateBack: () -> Unit
) {
    val isUpdate = wordId != null
    val wordState = viewModel.words.collectAsState().value.find { it.id == wordId }

    var wordText by remember { mutableStateOf(wordState?.word ?: "") }
    var phonetic by remember { mutableStateOf(wordState?.phonetic ?: "") }
    var partOfSpeech by remember { mutableStateOf(wordState?.partOfSpeech ?: "") }
    var englishDefinition by remember { mutableStateOf(wordState?.englishDefinition ?: "") }
    var vietnameseMeaning by remember { mutableStateOf(wordState?.vietnameseMeaning ?: "") }
    var context by remember { mutableStateOf(wordState?.context ?: "") }
    var collocation by remember { mutableStateOf("") } // Used in UI but maybe not in basic model

    Scaffold(
        containerColor = DeckColors.Background,
        topBar = {
            BunnyAppBar(title = "Bunny English", onBackClick = onNavigateBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = if (isUpdate) "Update Word" else "Add New Word",
                style = DeckTypography.headlineLg,
                color = DeckColors.OnSurface
            )
            Text(
                text = "Expand your vocabulary bit by bit.",
                style = DeckTypography.bodyMd,
                color = DeckColors.OnSurfaceVariant
            )

            // Basics Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DeckColors.PrimaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Create, contentDescription = null, tint = DeckColors.Primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Basics", style = DeckTypography.titleLg, color = DeckColors.Primary)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Word", style = DeckTypography.labelLg, color = DeckColors.OnSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = wordText,
                        onValueChange = { wordText = it },
                        placeholder = { Text("e.g. Ephemeral") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = DeckColors.SurfaceContainerLowest,
                            focusedContainerColor = DeckColors.SurfaceContainerLowest,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = DeckColors.Primary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Pronunciation", style = DeckTypography.labelLg, color = DeckColors.OnSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phonetic,
                        onValueChange = { phonetic = it },
                        placeholder = { Text("/əˈfemərəl/") },
                        trailingIcon = { Icon(Icons.Default.VolumeUp, contentDescription = null, tint = DeckColors.Primary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = DeckColors.SurfaceContainerLowest,
                            focusedContainerColor = DeckColors.SurfaceContainerLowest,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = DeckColors.Primary
                        )
                    )
                }
            }

            // Meaning Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DeckColors.SecondaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Translate, contentDescription = null, tint = DeckColors.Secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Meaning", style = DeckTypography.titleLg, color = DeckColors.Secondary)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Meaning (Vietnamese)", style = DeckTypography.labelLg, color = DeckColors.OnSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = vietnameseMeaning,
                        onValueChange = { vietnameseMeaning = it },
                        placeholder = { Text("Nghĩa của từ...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = DeckColors.SurfaceContainerLowest,
                            focusedContainerColor = DeckColors.SurfaceContainerLowest,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = DeckColors.Secondary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Description (English)", style = DeckTypography.labelLg, color = DeckColors.OnSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = englishDefinition,
                        onValueChange = { englishDefinition = it },
                        placeholder = { Text("Explain the concept in English...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = DeckColors.SurfaceContainerLowest,
                            focusedContainerColor = DeckColors.SurfaceContainerLowest,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = DeckColors.Secondary
                        )
                    )
                }
            }

            // Context Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DeckColors.TertiaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lightbulb, contentDescription = null, tint = DeckColors.OnTertiaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Context", style = DeckTypography.titleLg, color = DeckColors.OnTertiaryContainer)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Example Sentence", style = DeckTypography.labelLg, color = DeckColors.OnSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = context,
                        onValueChange = { context = it },
                        placeholder = { Text("How is this word used in a sentence?", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = DeckColors.SurfaceContainerLowest,
                            focusedContainerColor = DeckColors.SurfaceContainerLowest,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = DeckColors.TertiaryContainer
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Collocation", style = DeckTypography.labelLg, color = DeckColors.OnSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = collocation,
                        onValueChange = { collocation = it },
                        placeholder = { Text("Common word pairings...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = DeckColors.SurfaceContainerLowest,
                            focusedContainerColor = DeckColors.SurfaceContainerLowest,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = DeckColors.TertiaryContainer
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.background(DeckColors.TertiaryContainer, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                            Text("#Academic", style = DeckTypography.labelLg, color = DeckColors.OnTertiaryContainer)
                        }
                        Box(modifier = Modifier.background(DeckColors.TertiaryContainer, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                            Text("#Formal", style = DeckTypography.labelLg, color = DeckColors.OnTertiaryContainer)
                        }
                    }
                }
            }

            // Spacer for scroll and bottom image
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color.LightGray, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Notebook Image Placeholder", color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    val newWord = Word(
                        id = wordId ?: java.util.UUID.randomUUID().toString(),
                        deckId = deckId,
                        word = wordText,
                        phonetic = phonetic,
                        partOfSpeech = partOfSpeech,
                        englishDefinition = englishDefinition,
                        vietnameseMeaning = vietnameseMeaning,
                        context = context
                    )
                    if (isUpdate) {
                        viewModel.updateWord(newWord)
                    } else {
                        viewModel.addWord(newWord)
                    }
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
                Text("Lưu từ vựng", style = DeckTypography.titleLg)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
