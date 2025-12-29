package com.vasmarfas.mosstroiinformadmin.core.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Top-level функция для WASM js() вызова
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun openWindow(url: String) {
    js("window.open(url, '_blank')")
}

actual suspend fun downloadAndOpenFile(
    url: String,
    fileName: String,
    httpClient: HttpClient
): Result<Unit> {
    return try {
        // Для WASM просто открываем URL в новой вкладке
        withContext(Dispatchers.Default) {
            openWindow(url)
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

