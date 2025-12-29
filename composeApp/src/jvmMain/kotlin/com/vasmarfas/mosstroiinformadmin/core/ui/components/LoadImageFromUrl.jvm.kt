package com.vasmarfas.mosstroiinformadmin.core.ui.components

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skiko.toImage
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

actual suspend fun loadImageFromUrl(
    url: String,
    httpClient: HttpClient
): ImageBitmap? {
    return try {
        val bytes = withContext(Dispatchers.IO) {
            httpClient.get(url).body<ByteArray>()
        }
        val inputStream = ByteArrayInputStream(bytes)
        val bufferedImage: BufferedImage = ImageIO.read(inputStream)
        
        // Конвертируем BufferedImage в ImageBitmap через Skiko
        bufferedImage.toImage().toComposeImageBitmap()
    } catch (e: Exception) {
        null
    }
}

