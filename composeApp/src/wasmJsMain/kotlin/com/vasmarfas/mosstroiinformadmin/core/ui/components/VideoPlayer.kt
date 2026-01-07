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
 * ВРЕМЕННАЯ РЕАЛИЗАЦИЯ: VideoPlayer для WASM
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

    Box(modifier = modifier.fillMaxSize()) {
        DisposableEffect(url) {
            val containerId = "video-wasm-${url.hashCode()}"
            println("WASM VideoPlayer: Creating container with id: $containerId")
            
            // Формируем URL для MediaMTX с параметрами
            val mediaMtxUrl = buildString {
                append(url)
                if (!url.endsWith("/")) {
                    append("/")
                }
                append("?autoplay=true&muted=true&controls=true&playsinline=true")
            }
            
            println("WASM VideoPlayer: Loading MediaMTX URL: $mediaMtxUrl")
            
            // Создаем контейнер
            val container = document.createElement("div") as org.w3c.dom.HTMLDivElement
            container.id = containerId
            container.setAttribute("style", "position: absolute; top: 0; left: 0; width: 100%; height: 100%; z-index: 1000;")
            
            // Создаем iframe
            val iframe = document.createElement("iframe") as HTMLIFrameElement
            iframe.src = mediaMtxUrl
            iframe.setAttribute("style", "width: 100%; height: 100%; border: none; display: block;")
            iframe.setAttribute("allow", "autoplay; fullscreen; picture-in-picture")
            iframe.setAttribute("allowfullscreen", "true")
            iframe.setAttribute("frameborder", "0")
            
            iframe.addEventListener("load", { 
                println("WASM VideoPlayer: iframe loaded successfully for $mediaMtxUrl")
                hasError = false
            })
            
            iframe.addEventListener("error", { 
                println("WASM VideoPlayer: iframe error event for $mediaMtxUrl")
                hasError = true
                errorMessage = "Ошибка загрузки видео"
                onError?.invoke(errorMessage!!)
            })
            
            container.appendChild(iframe)
            document.body?.appendChild(container)
            
            println("WASM VideoPlayer: Container and iframe added to DOM")
            
            onDispose {
                println("WASM VideoPlayer: Disposing container: $containerId")
                document.getElementById(containerId)?.remove()
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
