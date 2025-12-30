package com.vasmarfas.mosstroiinformadmin.core.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual suspend fun downloadAndOpenFile(
    url: String,
    fileName: String,
    httpClient: HttpClient
): Result<Unit> {
    return try {
        // Для iOS просто открываем URL напрямую в браузере
        // Сохранение файла на iOS требует более сложной логики с UIDocumentPicker
        val nsUrl = NSURL(string = url)
        if (UIApplication.sharedApplication.canOpenURL(nsUrl)) {
            UIApplication.sharedApplication.openURL(nsUrl)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
