package com.minlish.app.presentation.deck

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minlish.app.data.source.remote.FirebaseAuthApi
import com.minlish.app.domain.model.Deck
import com.minlish.app.domain.model.Vocabulary
import com.minlish.app.domain.model.enumration.LearningGoal
import com.minlish.app.domain.repository.BunnyRepository
import com.minlish.app.domain.usecase.AutoFillWordUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.util.UUID
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DeckViewModel @Inject constructor(
    private val repository: BunnyRepository,
    private val autoFillWordUseCase: AutoFillWordUseCase,
    private val authApi: FirebaseAuthApi
) : ViewModel() {

    // Dùng MutableStateFlow nội bộ để có thể cập nhật danh sách ngay lập tức sau khi xóa
    private val _decks = MutableStateFlow<List<Deck>>(emptyList())
    val decks: StateFlow<List<Deck>> = _decks.asStateFlow()

    private val _currentDeck = MutableStateFlow<Deck?>(null)
    val currentDeck: StateFlow<Deck?> = _currentDeck.asStateFlow()

    private val _words = MutableStateFlow<List<Vocabulary>>(emptyList())
    val words: StateFlow<List<Vocabulary>> = _words.asStateFlow()

    private val _isSearchingAPI = MutableStateFlow(false)
    val isSearchingAPI: StateFlow<Boolean> = _isSearchingAPI.asStateFlow()

    private val _loadingProgress = MutableStateFlow<Map<String, Int>>(emptyMap())
    val loadingProgress: StateFlow<Map<String, Int>> = _loadingProgress.asStateFlow()

    init {
        syncDataIfNeeded()
        loadDecks()
    }

    /** Tải danh sách deck từ Firebase vào _decks. */
    fun loadDecks() {
        viewModelScope.launch {
            repository.getDecks().collect { list ->
                _decks.value = list
            }
        }
    }

    private fun syncDataIfNeeded() {
        viewModelScope.launch {
            try {
                repository.prepopulateInitialData()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun searchWordToAutoFill(query: String, partOfSpeech: String, onResult: (Vocabulary?) -> Unit) {
        viewModelScope.launch {
            _isSearchingAPI.value = true
            val wordData = autoFillWordUseCase(query, partOfSpeech)
            _isSearchingAPI.value = false
            onResult(wordData)
        }
    }

    fun searchDecks(query: String) {
        // Có thể filter Local state ở ListOfDeckScreen, không cần xử lý logic phức tạp ở đây
    }

    fun loadDeckDetails(deckId: String) {
        // Reset về null/empty ngay lập tức để tránh hiển thị dữ liệu cũ của deck trước
        _currentDeck.value = null
        _words.value = emptyList()

        viewModelScope.launch {
            val deck = repository.getDeckById(deckId)
            _currentDeck.value = deck

            if (deck != null) {
                // Deck do người dùng tạo (isPublic=false) mà chưa có từ vựng nào
                // thì không fallback sang từ vựng hệ thống — trả về danh sách rỗng ngay
                if (!deck.isPublic && deck.vocabularyIds.isEmpty()) {
                    _words.value = emptyList()
                    return@launch
                }
                repository.getVocabularyByDeck(deckId, deck.vocabularyIds).collectLatest { vocabList ->
                    _words.value = vocabList
                }
            }
        }
    }

    fun getDeckProgress(deckId: String): Flow<Int> {
        return repository.getProgress(deckId)
    }

    fun addWord(vocabulary: Vocabulary) {
        viewModelScope.launch {
            repository.insertVocabulary(vocabulary)
        }
    }

    fun updateWord(vocabulary: Vocabulary) {
        viewModelScope.launch {
            repository.insertVocabulary(vocabulary)
        }
    }

    fun createDeck(name: String, description: String, tags: List<String>) {
        viewModelScope.launch {
            val goalTags = tags.mapNotNull { tag ->
                try {
                    LearningGoal.valueOf(tag.uppercase())
                } catch (e: Exception) {
                    null
                }
            }
            val currentUserId = authApi.getCurrentUser()?.uid ?: ""
            val newDeck = Deck(
                id = UUID.randomUUID().toString(),
                name = name,
                description = description,
                tags = goalTags,
                createId = currentUserId,
                isPublic = false
            )
            // Optimistic update: hiển thị ngay trước khi Firebase xác nhận
            _decks.value = _decks.value + newDeck
            // Lưu lên Firebase, sau đó reload để đồng bộ dữ liệu chính xác từ server
            repository.insertDeck(newDeck)
            loadDecks()
        }
    }

    /**
     * Xóa deck do người dùng tạo (isPublic=false).
     * Cập nhật UI ngay lập tức bằng cách loại khỏi _decks trước,
     * sau đó xóa trên Firebase.
     */
    fun deleteDeck(deckId: String) {
        // Optimistic update: xóa ngay trên UI trước khi chờ Firebase
        _decks.value = _decks.value.filter { it.id != deckId }
        viewModelScope.launch {
            repository.deleteDeck(deckId)
        }
    }

    /** Xóa từ vựng khỏi deck do người dùng tạo, cập nhật danh sách ngay lập tức. */
    fun deleteVocabulary(deckId: String, vocabId: String) {
        // Optimistic update
        _words.value = _words.value.filter { it.id != vocabId }
        viewModelScope.launch {
            repository.deleteVocabulary(deckId, vocabId)
        }
    }

    /**
     * Xuất danh sách deck ra file CSV.
     * Mỗi deck là một section trong file.
     * @param decks Danh sách deck cần export
     * @param includePublic Có bao gồm deck chung (isPublic=true) không
     * @param context Context để ghi file
     * @param onComplete Callback trả về Uri của file đã tạo (null nếu lỗi)
     */
    fun exportDecksToCSV(
        decks: List<Deck>,
        includePublic: Boolean,
        context: Context,
        onComplete: (Uri?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val targetDecks = if (includePublic) decks else decks.filter { !it.isPublic }

                val sb = StringBuilder()
                sb.appendLine("MinLish Vocabulary Export")
                sb.appendLine("Exported: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}")
                sb.appendLine()

                for (deck in targetDecks) {
                    sb.appendLine("=== ${deck.name} (${if (deck.isPublic) "Chung" else "Của tôi"}) ===")
                    sb.appendLine("word,phonetic,partOfSpeech,englishDefinition,vietnameseMeaning,example,note")

                    // Lấy từ vựng cho từng deck
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
                            // Escape CSV: wrap in quotes nếu có dấu phẩy hoặc xuống dòng
                            if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
                                "\"${field.replace("\"", "\"\"")}\""
                            } else field
                        }
                        sb.appendLine(line)
                    }
                    sb.appendLine()
                }

                val fileName = "minlish_export_${System.currentTimeMillis()}.csv"
                val uri = saveCSVFile(context, fileName, sb.toString())
                onComplete(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(null)
            }
        }
    }

    private fun saveCSVFile(context: Context, fileName: String, content: String): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ dùng MediaStore
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.Downloads.MIME_TYPE, "text/csv")
                put(android.provider.MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { os ->
                    os.write(content.toByteArray(Charsets.UTF_8))
                }
                contentValues.clear()
                contentValues.put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(it, contentValues, null, null)
            }
            uri
        } else {
            // Android 9 trở xuống — lưu vào Downloads rồi dùng FileProvider
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsDir.mkdirs()
            val file = File(downloadsDir, fileName)
            FileWriter(file).use { it.write(content) }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        }
    }
}
