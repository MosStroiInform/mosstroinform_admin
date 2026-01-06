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
    imageCache[url]?.let { 
        println("LoadImageFromUrl: Using cached image for $url")
        return it 
    }
    
    return try {
        println("LoadImageFromUrl: Loading image from $url")
        val response = withContext(Dispatchers.IO) {
            // Используем переданный httpClient для всех URL
            // Ktor автоматически обрабатывает абсолютные URL
            httpClient.get(url)
        }
        println("LoadImageFromUrl: Response status: ${response.status}")
        
        if (!response.status.isSuccess()) {
            println("LoadImageFromUrl: Failed - status not success: ${response.status}")
            imageCache[url] = null
            return null
        }
        
        val bytes = response.body<ByteArray>()
        println("LoadImageFromUrl: Received ${bytes.size} bytes")
        
        if (bytes.isEmpty()) {
            println("LoadImageFromUrl: Failed - empty response")
            imageCache[url] = null
            return null
        }
        
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        println("LoadImageFromUrl: Bitmap decoded: ${bitmap != null}")
        
        if (bitmap == null) {
            println("LoadImageFromUrl: Failed - BitmapFactory returned null")
            imageCache[url] = null
            return null
        }
        
        val imageBitmap = bitmap.asImageBitmap()
        println("LoadImageFromUrl: ImageBitmap created successfully")
        
        // Сохраняем в кэш (даже если null, чтобы не пытаться загружать снова)
        imageCache[url] = imageBitmap
        
        imageBitmap
    } catch (e: Exception) {
        println("LoadImageFromUrl: Exception loading image: ${e.message}")
        e.printStackTrace()
        // Сохраняем null в кэш при ошибке
        imageCache[url] = null
        null
    }
}

