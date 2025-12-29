package com.vasmarfas.mosstroiinformadmin.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Типы размеров экрана
 */
enum class WindowSize {
    COMPACT,  // Телефоны в портретной ориентации (<600dp)
    MEDIUM,   // Планшеты в портретной ориентации, телефоны в ландшафте (600-840dp)
    EXPANDED  // Планшеты в ландшафте, десктопы (>840dp)
}

/**
 * Определение текущего размера окна
 */
@Composable
expect fun rememberWindowSize(): WindowSize

/**
 * Адаптивное значение в зависимости от размера экрана
 */
@Composable
fun <T> adaptiveValue(
    compact: T,
    medium: T = compact,
    expanded: T = medium
): T {
    return when (rememberWindowSize()) {
        WindowSize.COMPACT -> compact
        WindowSize.MEDIUM -> medium
        WindowSize.EXPANDED -> expanded
    }
}

/**
 * Адаптивные отступы
 */
object AdaptivePadding {
    val small: Dp
        @Composable get() = adaptiveValue(8.dp, 12.dp, 16.dp)
    
    val medium: Dp
        @Composable get() = adaptiveValue(16.dp, 20.dp, 24.dp)
    
    val large: Dp
        @Composable get() = adaptiveValue(24.dp, 32.dp, 40.dp)
    
    val extraLarge: Dp
        @Composable get() = adaptiveValue(32.dp, 48.dp, 64.dp)
}

/**
 * Адаптивные размеры карточек
 */
object AdaptiveCardSize {
    val maxWidth: Dp
        @Composable get() = adaptiveValue(
            compact = 600.dp,
            medium = 800.dp,
            expanded = 1200.dp
        )
    
    val gridColumns: Int
        @Composable get() = adaptiveValue(
            compact = 1,
            medium = 2,
            expanded = 3
        )
}

/**
 * Проверка, является ли экран компактным
 */
@Composable
fun isCompactScreen(): Boolean {
    return rememberWindowSize() == WindowSize.COMPACT
}

/**
 * Проверка, является ли экран расширенным
 */
@Composable
fun isExpandedScreen(): Boolean {
    return rememberWindowSize() == WindowSize.EXPANDED
}

