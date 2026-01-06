package com.vasmarfas.mosstroiinformadmin.core.ui.components

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image

// Максимальное количество попыток при SSL ошибках
private const val MAX_RETRIES = 3
// Задержка между попытками (в миллисекундах)
private const val RETRY_DELAY_MS = 500L

actual suspend fun loadImageFromUrl(
    url: String,
    httpClient: HttpClient
): ImageBitmap? {
    var lastException: Exception? = null
    for (attempt in 1..MAX_RETRIES) {
        try {
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
            return image.toComposeImageBitmap()
        } catch (e: Exception) {
            lastException = e
            val isSslError = e.message?.contains("SSL", ignoreCase = true) == true ||
                            e.message?.contains("ssl", ignoreCase = true) == true ||
                            e.message?.contains("TLS", ignoreCase = true) == true ||
                            e.message?.contains("tls", ignoreCase = true) == true ||
                            (e.cause?.message?.contains("SSL", ignoreCase = true) == true) ||
                            (e.cause?.message?.contains("ssl", ignoreCase = true) == true)
            
            if (isSslError && attempt < MAX_RETRIES) {
                delay(RETRY_DELAY_MS * attempt)
                continue
            } else {
                break
            }
        }
    }
    return null
}

