package com.minlish.app.presentation.deck.viewmodel

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minlish.app.core.util.FileStorageHelper
import com.minlish.app.domain.model.Deck
import com.minlish.app.domain.repository.BunnyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel cho màn hình [com.minlish.app.presentation.deck.ui.ListOfDeckScreen].
 * Chịu trách nhiệm:
 * - Tải và quan sát danh sách Deck.
 * - Xóa Deck (Optimistic Update).
 * - Export danh sách Deck ra file CSV.
 */
@HiltViewModel
class DeckListViewModel @Inject constructor(
    private val repository: BunnyRepository,
    private val fileStorageHelper: FileStorageHelper
) : ViewModel() {

    private val _decks = MutableStateFlow<List<Deck>>(emptyList())
    val decks: StateFlow<List<Deck>> = _decks.asStateFlow()

    init {
        loadDecks()
    }

    fun loadDecks() {
        viewModelScope.launch {
            repository.getDecks().collectLatest { list ->
                _decks.value = list
            }
        }
    }

    fun getDeckProgress(deckId: String): Flow<Int> = repository.getProgress(deckId)

    /**
     * Xóa Deck do người dùng tạo (isPublic = false).
     * Dùng Optimistic Update: cập nhật UI ngay lập tức, sau đó mới gọi Firebase.
     */
    fun deleteDeck(deckId: String) {
        _decks.value = _decks.value.filter { it.id != deckId }
        viewModelScope.launch {
            repository.deleteDeck(deckId)
        }
    }

    /**
     * Xuất danh sách Deck ra file CSV và chia sẻ qua Intent ACTION_SEND.
     * ViewModel chỉ xây dựng nội dung CSV — việc ghi file ủy quyền cho [FileStorageHelper].
     *
     * @param decks Danh sách Deck cần export.
     * @param includePublic Có bao gồm Deck hệ thống (isPublic=true) không.
     * @param onResult Callback trả về [Intent] chia sẻ (null nếu lỗi).
     */
    fun exportDecksToCSV(
        decks: List<Deck>,
        includePublic: Boolean,
        onResult: (Intent?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val targetDecks = if (includePublic) decks else decks.filter { !it.isPublic }
                val csvContent = buildCsvContent(targetDecks)
                val fileName = "minlish_export_${System.currentTimeMillis()}.csv"
                val uri: Uri? = fileStorageHelper.saveCSV(fileName, csvContent)

                if (uri != null) {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    onResult(shareIntent)
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }

    /** Xây dựng nội dung chuỗi CSV từ danh sách Deck. Logic thuần Kotlin, không cần Context. */
    private suspend fun buildCsvContent(targetDecks: List<Deck>): String {
        val sb = StringBuilder()
        val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        sb.appendLine("MinLish Vocabulary Export")
        sb.appendLine("Exported: $dateStr")
        sb.appendLine()

        for (deck in targetDecks) {
            sb.appendLine("=== ${deck.name} (${if (deck.isPublic) "Public" else "Mine"}) ===")
            sb.appendLine("word,phonetic,partOfSpeech,englishDefinition,vietnameseMeaning,example,note")

            val vocabs = try {
                repository.getVocabularyByDeck(deck.id, deck.vocabularyIds).first()
            } catch (e: Exception) {
                emptyList()
            }

            for (vocab in vocabs) {
                val line = listOf(
                    vocab.word,
                    vocab.phonetic,
                    vocab.partOfSpeech,
                    vocab.englishDefinition,
                    vocab.vietnameseMeaning,
                    vocab.context.replace("\n", " | "),
                    vocab.note
                ).joinToString(",") { field ->
                    if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
                        "\"${field.replace("\"", "\"\"")}\""
                    } else field
                }
                sb.appendLine(line)
            }
            sb.appendLine()
        }
        return sb.toString()
    }
}
