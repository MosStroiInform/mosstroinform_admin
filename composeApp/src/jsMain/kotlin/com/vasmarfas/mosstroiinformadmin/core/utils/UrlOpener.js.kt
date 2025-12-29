package com.vasmarfas.mosstroiinformadmin.core.utils

actual fun openUrlInBrowser(url: String) {
    kotlinx.browser.window.open(url, "_blank")
}

