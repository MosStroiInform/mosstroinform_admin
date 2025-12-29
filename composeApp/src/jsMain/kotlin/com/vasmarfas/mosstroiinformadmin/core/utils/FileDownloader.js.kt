package com.vasmarfas.mosstroiinformadmin.core.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.HTMLAnchorElement
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlinx.browser.document
import kotlinx.browser.window

actual suspend fun downloadAndOpenFile(
    url: String,
    fileName: String,
    httpClient: HttpClient
): Result<Unit> {
    return try {
        val bytes = withContext(Dispatchers.Default) {
            httpClient.get(url).body<ByteArray>()
        }
        
        val blob = Blob(arrayOf(bytes), BlobPropertyBag("application/octet-stream"))
        val blobUrl = js("URL.createObjectURL(blob)") as String
        
        val anchor = document.createElement("a") as HTMLAnchorElement
        anchor.href = blobUrl
        anchor.download = fileName
        anchor.style.display = "none"
        document.body?.appendChild(anchor)
        anchor.click()
        document.body?.removeChild(anchor)
        
        js("URL.revokeObjectURL(blobUrl)")
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

