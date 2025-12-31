package com.vasmarfas.mosstroiinformadmin.core.utils

import android.content.Intent
import android.net.Uri
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri

actual suspend fun downloadAndOpenFile(
    url: String,
    fileName: String,
    httpClient: HttpClient,
): Result<Unit> {
    return try {
        val context = com.vasmarfas.mosstroiinformadmin.core.utils.getApplicationContext()
        
        // Просто открываем URL напрямую через браузер или внешнее приложение
        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        withContext(Dispatchers.Main) {
            context.startActivity(intent)
        }
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

private fun getMimeType(fileName: String): String {
    return when (fileName.substringAfterLast('.', "").lowercase()) {
        "pdf" -> "application/pdf"
        "doc", "docx" -> "application/msword"
        "xls", "xlsx" -> "application/vnd.ms-excel"
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        else -> "application/octet-stream"
    }
}

