package com.vasmarfas.mosstroiinformadmin.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun rememberWindowSize(): WindowSize {
    val windowInfo = LocalWindowInfo.current
    val width = windowInfo.containerSize.width
    
    return when {
        width < 600 -> WindowSize.COMPACT
        width < 840 -> WindowSize.MEDIUM
        else -> WindowSize.EXPANDED
    }
}

