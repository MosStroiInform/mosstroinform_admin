package com.vasmarfas.mosstroiinformadmin.core.ui.components

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

// Кэш для загруженных изображений (в памяти)
private val imageCache = mutableMapOf<String, ImageBitmap?>()

actual suspend fun loadImageFromUrl(
    url: String,
    httpClient: HttpClient
): ImageBitmap? {
    // Проверяем кэш
    imageCache[url]?.let { return it }
    
    return try {
        val response = withContext(Dispatchers.IO) {
            // Используем переданный httpClient для всех URL
            // Ktor автоматически обрабатывает абсолютные URL
            httpClient.get(url)
        }
        if (!response.status.isSuccess()) {
            imageCache[url] = null
            return null
        }
        
        val bytes = response.body<ByteArray>()
        
        if (bytes.isEmpty()) {
            imageCache[url] = null
            return null
        }
        
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val imageBitmap = bitmap?.asImageBitmap()
        
        // Сохраняем в кэш (даже если null, чтобы не пытаться загружать снова)
        imageCache[url] = imageBitmap
        
        imageBitmap
    } catch (e: Exception) {
        // Сохраняем null в кэш при ошибке
        imageCache[url] = null
        null
    }
}

