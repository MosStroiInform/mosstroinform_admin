package com.vasmarfas.mosstroiinformadmin.core.utils

import java.awt.Desktop

actual fun openUrlInBrowser(url: String) {
    if (Desktop.isDesktopSupported()) {
        Desktop.getDesktop().browse(java.net.URI(url))
    }
}

