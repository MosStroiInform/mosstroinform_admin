package com.vasmarfas.mosstroiinformadmin.core.ui

import androidx.compose.runtime.*

@Composable
actual fun rememberWindowSize(): WindowSize {
    // Для WASM используем простую логику - всегда расширенный режим
    // так как WASM обычно используется на больших экранах
    return WindowSize.EXPANDED
}

