package com.vasmarfas.mosstroiinformadmin.core.ui.components

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
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
            // Для WASM используем короткий таймаут и обработку ошибок, чтобы избежать зависания при CORS
            // Используем withTimeoutOrNull для гарантированного завершения даже при CORS ошибках
            val bytes = withTimeoutOrNull(5000) {
                try {
                    withContext(Dispatchers.Default) {
                        // Используем httpClient напрямую
                        // Если возникнет CORS ошибка, таймаут прервет выполнение
                        httpClient.get(url).body<ByteArray>()
                    }
                } catch (e: Exception) {
                    // Любая ошибка (включая CORS) - возвращаем null
                    null
                }
            }
            
            if (bytes == null || bytes.isEmpty()) {
                if (attempt < MAX_RETRIES) {
                    delay(RETRY_DELAY_MS * attempt)
                    continue
                }
                return null
            }
            
            // Используем Skia для декодирования изображения
            val image = Image.makeFromEncoded(bytes)
            // В wasmJs конвертируем через toComposeImageBitmap из androidx.compose.ui.graphics
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
    // Игнорируем все ошибки при загрузке изображений, чтобы не зависать
    return null
}

