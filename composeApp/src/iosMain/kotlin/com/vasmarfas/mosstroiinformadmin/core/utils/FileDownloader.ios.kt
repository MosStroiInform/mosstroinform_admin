package com.vasmarfas.mosstroiinformadmin.core.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual suspend fun downloadAndOpenFile(
    url: String,
    fileName: String,
    httpClient: HttpClient
): Result<Unit> {
    return try {
        val bytes = withContext(Dispatchers.Default) {
            httpClient.get(url).body<ByteArray>()
        }
        
        if (bytes.isNotEmpty()) {
            val fileManager = NSFileManager.defaultManager
            val tempDir = fileManager.temporaryDirectory
            val fileUrl = tempDir.URLByAppendingPathComponent(fileName)
            
            // Создаем NSData из байтов
            val nsData = NSData.create(bytes = bytes)
            
            // Записываем данные в файл
            nsData.writeToURL(fileUrl, atomically = true)
            
            // Открываем файл через системный диалог
            if (UIApplication.sharedApplication.canOpenURL(fileUrl)) {
                UIApplication.sharedApplication.openURL(fileUrl)
            }
        }
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
