package com.minlish.app.core.util

import android.net.Uri

/**
 * Helper interface để ghi file lên bộ nhớ thiết bị.
 * Tách logic file I/O ra khỏi ViewModel để ViewModel không
 * phụ thuộc vào Android Context (tuân thủ Clean Architecture).
 */
interface FileStorageHelper {
    /**
     * Lưu nội dung [content] thành file CSV với tên [fileName]
     * vào thư mục Downloads của thiết bị.
     * @return URI của file đã tạo, hoặc null nếu thất bại.
     */
    fun saveCSV(fileName: String, content: String): Uri?
}
