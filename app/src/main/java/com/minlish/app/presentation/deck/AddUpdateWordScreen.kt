package com.minlish.app.presentation.deck

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.minlish.app.domain.model.Vocabulary
import com.minlish.app.presentation.common.component.BunnyAppBar
import com.minlish.app.ui.theme.DeckColors
import com.minlish.app.ui.theme.DeckTypography
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
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
    var partOfSpeech by remember { mutableStateOf(wordState?.partOfSpeech ?: "noun") }
    var englishDefinition by remember { mutableStateOf(wordState?.englishDefinition ?: "") }
    var vietnameseMeaning by remember { mutableStateOf(wordState?.vietnameseMeaning ?: "") }
    var contextExample by remember { mutableStateOf(wordState?.context ?: "") }
    var soundUrl by remember { mutableStateOf(wordState?.soundUrl ?: "") }

    val isSearching by viewModel.isSearchingAPI.collectAsState()
    val contextCtx = LocalContext.current

    val partsOfSpeech = listOf("noun", "verb", "adjective", "adverb", "pronoun", "preposition", "conjunction", "interjection")
    var expandedDropdown by remember { mutableStateOf(false) }

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
                    
                    Text("Part of Speech", style = DeckTypography.labelLg, color = DeckColors.OnSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = !expandedDropdown }
                    ) {
                        OutlinedTextField(
                            value = partOfSpeech,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = DeckColors.SurfaceContainerLowest,
                                focusedContainerColor = DeckColors.SurfaceContainerLowest,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = DeckColors.Primary
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            partsOfSpeech.forEach { pos ->
                                DropdownMenuItem(
                                    text = { Text(pos) },
                                    onClick = {
                                        partOfSpeech = pos
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (wordText.isBlank()) return@Button
                            viewModel.searchWordToAutoFill(wordText, partOfSpeech) { fetchedWord ->
                                if (fetchedWord != null) {
                                    phonetic = fetchedWord.phonetic
                                    englishDefinition = fetchedWord.englishDefinition
                                    vietnameseMeaning = fetchedWord.vietnameseMeaning
                                    contextExample = fetchedWord.context
                                    soundUrl = fetchedWord.soundUrl
                                    Toast.makeText(contextCtx, "Tự động điền thành công!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(contextCtx, "Không tìm thấy loại từ này", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeckColors.Primary)
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = DeckColors.SurfaceContainerLowest)
                        } else {
                            Icon(Icons.Default.Search, contentDescription = "Auto Fill")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Auto-fill from API")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Pronunciation", style = DeckTypography.labelLg, color = DeckColors.OnSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phonetic,
                        onValueChange = { phonetic = it },
                        readOnly = true,
                        placeholder = { Text("/əˈfemərəl/") },
                        trailingIcon = {
                            IconButton(onClick = { 
                                if (soundUrl.isNotBlank()) {
                                    playAudio(contextCtx, soundUrl)
                                } else {
                                    Toast.makeText(contextCtx, "Chưa có file phát âm cho từ này", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Play Pronunciation", tint = DeckColors.Primary)
                            }
                        },
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
                        value = contextExample,
                        onValueChange = { contextExample = it },
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
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    val newWord = Vocabulary(
                        id = wordId ?: UUID.randomUUID().toString(),
                        deckId = deckId,
                        word = wordText,
                        phonetic = phonetic,
                        partOfSpeech = partOfSpeech,
                        soundUrl = soundUrl,
                        level = null,
                        englishDefinition = englishDefinition,
                        vietnameseMeaning = vietnameseMeaning,
                        context = contextExample
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
