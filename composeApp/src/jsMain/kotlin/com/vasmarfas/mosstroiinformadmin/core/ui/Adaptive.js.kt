package com.vasmarfas.mosstroiinformadmin.core.ui

import androidx.compose.runtime.*
import kotlinx.browser.window

@Composable
actual fun rememberWindowSize(): WindowSize {
    var size by remember { mutableStateOf(calculateWindowSize()) }
    
    LaunchedEffect(Unit) {
        window.addEventListener("resize", {
            size = calculateWindowSize()
        })
    }
    
    return size
}

private fun calculateWindowSize(): WindowSize {
    val width = window.innerWidth
    return when {
        width < 600 -> WindowSize.COMPACT
        width < 840 -> WindowSize.MEDIUM
        else -> WindowSize.EXPANDED
    }
}

