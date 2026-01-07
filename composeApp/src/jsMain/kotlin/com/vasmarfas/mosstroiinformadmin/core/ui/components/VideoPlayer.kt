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
 * ВРЕМЕННАЯ РЕАЛИЗАЦИЯ: VideoPlayer для Web (JS)
 * Открывает ссылку в браузере по нажатию кнопки
 * 
 * TODO: Раскомментировать рабочий код ниже после исправления WebRTC
 */
@Composable
actual fun VideoPlayer(
    url: String,
    modifier: Modifier,
    autoPlay: Boolean,
    muted: Boolean,
    onError: ((String) -> Unit)?
) {
    // Для web/open-in-browser используем преобразованную ссылку
    val resolvedUrl = remember(url) { adjustStreamUrlForNonAndroid(url) }

    // ВРЕМЕННАЯ РЕАЛИЗАЦИЯ: кнопка для открытия в браузере
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
    
    /* РАБОЧИЙ КОД - ЗАКОММЕНТИРОВАН ДЛЯ ВРЕМЕННОГО РЕЛИЗА
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(url) {
        hasError = false
        errorMessage = null
    }

    // Для Web используем iframe для встраивания видео потока
    // Медиа-сервер предоставляет WebRTC через HTTPS, который можно встроить в iframe
    Box(modifier = modifier) {
        DisposableEffect(url) {
            val containerId = "video-iframe-${url.hashCode()}"
            var container = document.getElementById(containerId) as? org.w3c.dom.HTMLDivElement
            
            if (container == null) {
                container = document.createElement("div") as org.w3c.dom.HTMLDivElement
                container.id = containerId
                container.setAttribute("style", "width: 100%; height: 100%; position: relative; background: #000;")
                
                val iframe = document.createElement("iframe") as HTMLIFrameElement
                iframe.src = url
                iframe.setAttribute("style", "width: 100%; height: 100%; border: none;")
                iframe.setAttribute("allow", "autoplay; fullscreen; picture-in-picture")
                iframe.setAttribute("allowfullscreen", "true")
                
                // Слушаем ошибки загрузки iframe
                iframe.onload = {
                    // Iframe загружен успешно
                    hasError = false
                }
                
                container.appendChild(iframe)
                
                // Добавляем контейнер в DOM
                val root = document.getElementById("root") ?: document.body
                root?.appendChild(container)
            } else {
                // Обновляем URL если изменился
                val iframe = container.querySelector("iframe") as? HTMLIFrameElement
                if (iframe != null && iframe.src != url) {
                    iframe.src = url
                }
            }
            
            onDispose {
                // Не удаляем контейнер полностью, так как он может использоваться Compose
                // Просто очищаем содержимое при необходимости
            }
        }

        // Показываем ошибку если есть
        if (hasError && errorMessage != null) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Ошибка загрузки видео",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "Неизвестная ошибка",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
    */
}
