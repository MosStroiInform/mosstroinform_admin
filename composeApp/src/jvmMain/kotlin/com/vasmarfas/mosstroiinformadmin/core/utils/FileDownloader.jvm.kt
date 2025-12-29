package com.vasmarfas.mosstroiinformadmin.core.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths

actual suspend fun downloadAndOpenFile(
    url: String,
    fileName: String,
    httpClient: HttpClient
): Result<Unit> {
    return try {
        val bytes = withContext(Dispatchers.IO) {
            httpClient.get(url).body<ByteArray>()
        }
        
        val tempDir = System.getProperty("java.io.tmpdir")
        val file = File(tempDir, fileName)
        FileOutputStream(file).use { it.write(bytes) }
        
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file)
        } else {
            // Fallback: открыть папку с файлом
            val filePath = file.absolutePath
            val parentDir = File(filePath).parent
            Desktop.getDesktop().open(File(parentDir))
        }
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

