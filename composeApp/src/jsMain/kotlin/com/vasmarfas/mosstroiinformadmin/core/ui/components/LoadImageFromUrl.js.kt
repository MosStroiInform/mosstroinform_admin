package com.vasmarfas.mosstroiinformadmin.core.ui.components

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image

actual suspend fun loadImageFromUrl(
    url: String,
    httpClient: HttpClient
): ImageBitmap? {
    return try {
        val bytes = withContext(Dispatchers.Default) {
            // Для абсолютных URL используем прямой запрос
            // Ktor автоматически обрабатывает абсолютные URL
            httpClient.get(url).body<ByteArray>()
        }
        
        if (bytes.isEmpty()) {
            return null
        }
        
        // Используем Skia для декодирования изображения
        val image = Image.makeFromEncoded(bytes)
        // В JS используем toComposeImageBitmap из androidx.compose.ui.graphics
        image.toComposeImageBitmap()
    } catch (e: Exception) {
        null
    }
}

