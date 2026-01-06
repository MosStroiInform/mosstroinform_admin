package com.vasmarfas.mosstroiinformadmin.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Ожидаемый интерфейс для видеоплеера
 * Реализуется через expect/actual для каждой платформы
 */
@Composable
expect fun VideoPlayer(
    url: String,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true,
    muted: Boolean = false,
    onError: ((String) -> Unit)? = null
)


