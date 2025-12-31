package com.vasmarfas.mosstroiinformadmin.core.ui.components

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.skia.Image

actual suspend fun loadImageFromUrl(
    url: String,
    httpClient: HttpClient
): ImageBitmap? {
    return try {
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
            return null
        }
        
        // Используем Skia для декодирования изображения
        val image = Image.makeFromEncoded(bytes)
        // В wasmJs конвертируем через toComposeImageBitmap из androidx.compose.ui.graphics
        image.toComposeImageBitmap()
    } catch (e: Exception) {
        // Игнорируем все ошибки при загрузке изображений, чтобы не зависать
        null
    }
}

