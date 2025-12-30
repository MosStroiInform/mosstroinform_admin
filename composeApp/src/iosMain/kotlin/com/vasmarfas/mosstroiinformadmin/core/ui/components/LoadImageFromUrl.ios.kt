package com.vasmarfas.mosstroiinformadmin.core.ui.components

import androidx.compose.ui.graphics.ImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skiko.toImageBitmap
import platform.UIKit.UIImage
import platform.Foundation.NSData
import platform.Foundation.NSMutableData
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.addressOf

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
        
        // Используем UIImage для декодирования на iOS
        // Создаем NSData из ByteArray через usePinned и NSMutableData
        val nsData = bytes.usePinned { pinned ->
            val mutableData = NSMutableData()
            mutableData.appendBytes(pinned.addressOf(0), bytes.size.toULong())
            mutableData as NSData
        }
        val uiImage = UIImage(data = nsData)
        
        // Конвертируем UIImage в ImageBitmap через Skiko
        uiImage?.toImageBitmap()
    } catch (e: Exception) {
        null
    }
}

