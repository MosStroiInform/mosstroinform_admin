package com.vasmarfas.mosstroiinformadmin.core.ui.components

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jetbrains.skiko.toImage
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import javax.net.ssl.SSLException

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
            val bytes = withContext(Dispatchers.IO) {
                httpClient.get(url).body<ByteArray>()
            }
            val inputStream = ByteArrayInputStream(bytes)
            val bufferedImage: BufferedImage = ImageIO.read(inputStream)
            
            // Конвертируем BufferedImage в ImageBitmap через Skiko
            return bufferedImage.toImage().toComposeImageBitmap()
        } catch (e: Exception) {
            lastException = e
            val isSslError = e is SSLException || 
                            e.message?.contains("SSL", ignoreCase = true) == true ||
                            e.message?.contains("ssl", ignoreCase = true) == true ||
                            e.cause is SSLException ||
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

