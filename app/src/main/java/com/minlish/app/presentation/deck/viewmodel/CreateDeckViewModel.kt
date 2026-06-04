package com.minlish.app.presentation.deck.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minlish.app.domain.model.Deck
import com.minlish.app.domain.usecase.CreateDeckUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel cho màn hình [com.minlish.app.presentation.deck.ui.CreateNewDeckScreen].
 * Chịu trách nhiệm:
 * - Ủy quyền toàn bộ business logic tạo Deck cho [CreateDeckUseCase].
 * - Quản lý trạng thái đang lưu (isSaving) để disable nút bấm.
 * - Phát sự kiện [deckCreatedEvent] để UI điều hướng về sau khi Firebase xác nhận lưu thành công.
 */
@HiltViewModel
class CreateDeckViewModel @Inject constructor(
    private val createDeckUseCase: CreateDeckUseCase
) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    /**
     * SharedFlow phát một lần sau khi Deck được lưu lên Firebase thành công.
     * UI lắng nghe event này để navigate back đúng thời điểm,
     * tránh race condition (navigate trước khi data đã được ghi).
     */
    private val _deckCreatedEvent = MutableSharedFlow<Deck>(extraBufferCapacity = 1)
    val deckCreatedEvent: SharedFlow<Deck> = _deckCreatedEvent.asSharedFlow()

    /**
     * Tạo Deck mới. Toàn bộ business logic (UUID, userId, tag mapping)
     * được thực hiện trong [CreateDeckUseCase] — ViewModel chỉ điều phối.
     */
    fun createDeck(name: String, description: String, tags: List<String>) {
        if (name.isBlank() || _isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val newDeck = createDeckUseCase(name, description, tags)
                _deckCreatedEvent.emit(newDeck)
            } finally {
                _isSaving.value = false
            }
        }
    }
}
