package com.vasmarfas.mosstroiinformadmin.core.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
actual fun rememberWindowSize(): WindowSize {
    val activity = LocalActivity.current as? ComponentActivity
        ?: return WindowSize.COMPACT
    
    val windowSizeClass = calculateWindowSizeClass(activity)
    return when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> WindowSize.COMPACT
        WindowWidthSizeClass.Medium -> WindowSize.MEDIUM
        WindowWidthSizeClass.Expanded -> WindowSize.EXPANDED
        else -> WindowSize.COMPACT
    }
}

