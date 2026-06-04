package com.minlish.app.data

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.minlish.app.core.util.FileStorageHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileWriter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Triển khai [FileStorageHelper] sử dụng [ApplicationContext].
 * Xử lý sự khác biệt giữa Android API 29+ (MediaStore) và API cũ hơn (FileProvider).
 * Được inject qua Hilt, không bao giờ truyền Context trực tiếp qua UI.
 */
@Singleton
class FileStorageHelperImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FileStorageHelper {

    override fun saveCSV(fileName: String, content: String): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStore(fileName, content)
        } else {
            saveViaFileProvider(fileName, content)
        }
    }

    /** Android 10+: Dùng MediaStore để ghi vào Downloads mà không cần permission. */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveViaMediaStore(fileName: String, content: String): Uri? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(android.provider.MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(android.provider.MediaStore.Downloads.MIME_TYPE, "text/csv")
            put(android.provider.MediaStore.Downloads.IS_PENDING, 1)
        }
        val uri = resolver.insert(
            android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        )
        uri?.let {
            resolver.openOutputStream(it)?.use { os ->
                os.write(content.toByteArray(Charsets.UTF_8))
            }
            contentValues.clear()
            contentValues.put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(it, contentValues, null, null)
        }
        return uri
    }

    /** Android 9 trở xuống: Ghi vào Downloads rồi dùng FileProvider để tạo URI chia sẻ an toàn. */
    private fun saveViaFileProvider(fileName: String, content: String): Uri? {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            downloadsDir.mkdirs()
            val file = File(downloadsDir, fileName)
            FileWriter(file).use { it.write(content) }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
