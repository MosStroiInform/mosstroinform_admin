package com.vasmarfas.mosstroiinformadmin.core.utils

// Top-level функция для WASM js() вызова
@OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
private fun openWindow(url: String) {
    js("window.open(url, '_blank')")
}

actual fun openUrlInBrowser(url: String) {
    openWindow(url)
}

