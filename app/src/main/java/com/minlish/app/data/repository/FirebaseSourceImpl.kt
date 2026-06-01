package com.minlish.app.data.repository

import com.minlish.app.data.source.remote.FirebaseSource
import com.minlish.app.domain.model.Vocabulary

class FirebaseSourceImpl : FirebaseSource {
    override suspend fun getWordFromSystem(word: String): Vocabulary? {
        // TODO: Kết nối Firebase thật ở đây để lấy từ vựng.
        // Hiện tại giả lập Firebase không có từ vựng cần tìm, trả về null để flow tiếp tục chuyển sang gọi 2 API.
        return null 
    }
}