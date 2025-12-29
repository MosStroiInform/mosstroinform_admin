package com.vasmarfas.mosstroiinformadmin.core.ui.components

import androidx.compose.ui.graphics.ImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.UIKit.UIImage
import platform.Foundation.NSData
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.NSURL
import org.jetbrains.skiko.toImageBitmap

actual suspend fun loadImageFromUrl(
    url: String,
    httpClient: HttpClient
): ImageBitmap? {
    return try {
        val nsUrl = NSURL(string = url)
        val data: NSData? = withContext(Dispatchers.Default) {
            NSData.dataWithContentsOfURL(nsUrl)
        }
        if (data != null) {
            val uiImage = UIImage(data = data)
            uiImage?.toImageBitmap()
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

