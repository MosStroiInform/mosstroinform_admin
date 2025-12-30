package com.vasmarfas.mosstroiinformadmin.core.ui.components

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.OptIn

actual suspend fun loadImageFromUrl(
    url: String,
    httpClient: HttpClient
): ImageBitmap? {
    return try {
        val bytes = withContext(Dispatchers.Default) {
            httpClient.get(url).body<ByteArray>()
        }
        
        if (bytes.isEmpty()) {
            return null
        }
        
        // Используем Skia для декодирования изображения (как в JS/WASM)
        val image = Image.makeFromEncoded(bytes)
        image.toComposeImageBitmap()
    } catch (e: Exception) {
        null
    }
}
