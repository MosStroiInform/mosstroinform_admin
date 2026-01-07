package com.vasmarfas.mosstroiinformadmin.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.vasmarfas.mosstroiinformadmin.core.utils.adjustStreamUrlForNonAndroid
import com.vasmarfas.mosstroiinformadmin.core.utils.openUrlInBrowser

/**
 * Реализация VideoPlayer для JVM (Desktop)
 * Для JVM используем fallback на браузер, так как интеграция JavaFX с Compose Desktop
 * требует сложной настройки и может быть нестабильной
 */
@Composable
actual fun VideoPlayer(
    url: String,
    modifier: Modifier,
    autoPlay: Boolean,
    muted: Boolean,
    onError: ((String) -> Unit)?
) {
    val resolvedUrl = remember(url) { adjustStreamUrlForNonAndroid(url) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Видеопоток",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Для просмотра видео откройте ссылку в браузере",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    openUrlInBrowser(resolvedUrl)
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text("Открыть в браузере")
            }
        }
    }
}

