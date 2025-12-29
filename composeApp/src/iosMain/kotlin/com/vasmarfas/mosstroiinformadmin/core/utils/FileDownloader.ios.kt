package com.vasmarfas.mosstroiinformadmin.core.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenURLOptionsKeyAnnotation
import platform.UIKit.openURL

actual suspend fun downloadAndOpenFile(
    url: String,
    fileName: String,
    httpClient: HttpClient
): Result<Unit> {
    return try {
        val nsUrl = NSURL(string = url)
        val data: NSData? = withContext(Dispatchers.Default) {
            NSData.dataWithContentsOfURL(nsUrl)
        }
        
        if (data != null) {
            val fileManager = NSFileManager.defaultManager
            val tempDir = fileManager.temporaryDirectory
            val fileUrl = tempDir.URLByAppendingPathComponent(fileName)
            
            data.writeToURL(fileUrl, true, null)
            
            // Открываем файл через системный диалог
            UIApplication.sharedApplication.openURL(
                fileUrl,
                options = mapOf(),
                completionHandler = null
            )
        }
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

