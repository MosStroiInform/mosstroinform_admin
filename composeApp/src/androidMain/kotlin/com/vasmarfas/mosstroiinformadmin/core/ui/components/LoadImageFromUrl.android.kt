package com.vasmarfas.mosstroiinformadmin.core.ui.components

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.vasmarfas.mosstroiinformadmin.core.utils.getApplicationContext
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

// Кэш для загруженных изображений (в памяти)
private val imageCache = mutableMapOf<String, ImageBitmap?>()

// Mutex для синхронизации запросов к одному хосту (избегаем конфликтов SSL при параллельных запросах)
private val hostMutexes = mutableMapOf<String, Mutex>()
private val mutexMapMutex = Mutex()

// Получаем или создаем Mutex для конкретного хоста
private suspend fun getHostMutex(url: String): Mutex {
    val host = try {
        java.net.URL(url).host
    } catch (e: Exception) {
        "default"
    }
    
    return mutexMapMutex.withLock {
        hostMutexes.getOrPut(host) { Mutex() }
    }
}

// Coil ImageLoader с оптимизированными настройками для работы с SSL
private val imageLoader: ImageLoader by lazy {
    ImageLoader.Builder(getApplicationContext())
        .crossfade(false) // Отключаем анимацию для быстрой загрузки
        .respectCacheHeaders(false) // Игнорируем заголовки кеша
        .build()
}

actual suspend fun loadImageFromUrl(
    url: String,
    httpClient: HttpClient
): ImageBitmap? {
    // Проверяем кэш
    imageCache[url]?.let { 
        println("LoadImageFromUrl: Using cached image for $url")
        return it 
    }
    
    // Синхронизируем запросы к одному хосту через Mutex
    val hostMutex = getHostMutex(url)
    
    return hostMutex.withLock {
        // Проверяем кэш еще раз после получения блокировки
        imageCache[url]?.let {
            println("LoadImageFromUrl: Using cached image for $url (after lock)")
            return@withLock it
        }
        
        try {
            println("LoadImageFromUrl: Loading image from $url using Coil")
            
            // Используем Coil для загрузки изображения
            // Coil имеет встроенную обработку SSL и работает надежнее, чем прямые HTTP запросы
            val request = ImageRequest.Builder(getApplicationContext())
                .data(url)
                .allowHardware(false) // Отключаем hardware bitmap для совместимости
                .build()
            
            val result = withContext(Dispatchers.IO) {
                imageLoader.execute(request)
            }
            
            if (result is SuccessResult) {
                // Получаем Bitmap из Drawable
                val drawable = result.drawable
                val bitmap = when (drawable) {
                    is android.graphics.drawable.BitmapDrawable -> {
                        drawable.bitmap
                    }
                    else -> {
                        // Конвертируем любой Drawable в Bitmap
                        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 1024
                        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 1024
                        val bmp = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(bmp)
                        drawable.setBounds(0, 0, canvas.width, canvas.height)
                        drawable.draw(canvas)
                        bmp
                    }
                }
                
                val imageBitmap = bitmap.asImageBitmap()
                // Сохраняем в кэш
                imageCache[url] = imageBitmap
                
                imageBitmap
            } else {
                imageCache[url] = null
                null
            }
        } catch (e: Exception) {
            println("LoadImageFromUrl: Exception loading image: ${e.message}")
            e.printStackTrace()
            
            // Сохраняем null в кэш при ошибке
            imageCache[url] = null
            null
        }
    }
}
