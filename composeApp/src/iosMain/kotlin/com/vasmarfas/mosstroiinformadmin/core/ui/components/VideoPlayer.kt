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
 * ВРЕМЕННАЯ РЕАЛИЗАЦИЯ: VideoPlayer для iOS
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

    UIKitView(
        factory = {
            val playerViewController = AVPlayerViewController()
            val videoURL = NSURL(string = url)
            val player = AVPlayer(URL = videoURL)
            
            playerViewController.player = player
            
            if (muted) {
                player.muted = true
            }
            
            if (autoPlay) {
                player.play()
            }
            
            playerViewController.view
        },
        modifier = modifier,
        update = { view ->
            // Обновление при изменении URL
            val playerViewController = view.nextResponder as? AVPlayerViewController
            if (playerViewController != null) {
                val videoURL = NSURL(string = url)
                val player = AVPlayer(URL = videoURL)
                playerViewController.player = player
                
                if (muted) {
                    player.muted = true
                }
                
                if (autoPlay) {
                    player.play()
                }
            }
        }
    )

    if (hasError && errorMessage != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
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
                        imageVector = Icons.Filled.Error,
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
