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
import com.vasmarfas.mosstroiinformadmin.core.utils.openUrlInBrowser

/**
 * ВРЕМЕННАЯ РЕАЛИЗАЦИЯ: VideoPlayer для Android
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
                    openUrlInBrowser(url)
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
    
    // Всегда используем WebView для порта 8889 (MediaMTX WebRTC)
    // ExoPlayer не поддерживает WebRTC напрямую
    val useWebView = remember(url) {
        url.contains(":8889") || url.contains("/vid") || (!url.endsWith(".m3u8") && !url.contains("/hls/") && !url.endsWith(".mp4") && !url.endsWith(".webm") && !url.endsWith(".avi"))
    }

    LaunchedEffect(url) {
        hasError = false
        errorMessage = null
    }

    if (useWebView) {
        // Используем WebView для WebRTC потоков через MediaMTX
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
                    settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    // WebRTC требует hardware acceleration
                    setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                    
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            println("Android WebView: Page loaded: $url")
                        }
                        
                        override fun onReceivedError(
                            view: WebView?,
                            request: android.webkit.WebResourceRequest?,
                            error: android.webkit.WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            println("Android WebView: Error loading page: ${error?.description}")
                            hasError = true
                            errorMessage = "Ошибка загрузки: ${error?.description}"
                            onError?.invoke(errorMessage ?: "Неизвестная ошибка")
                        }
                        
                        override fun onReceivedHttpError(
                            view: WebView?,
                            request: android.webkit.WebResourceRequest?,
                            errorResponse: android.webkit.WebResourceResponse?
                        ) {
                            super.onReceivedHttpError(view, request, errorResponse)
                            val statusCode = errorResponse?.statusCode ?: 0
                            val url = request?.url?.toString() ?: "unknown"
                            println("Android WebView: HTTP error $statusCode for URL: $url")
                            
                            // Игнорируем 404 для /whep endpoint - это нормально, MediaMTX создает его динамически
                            if (statusCode == 404 && url.contains("/whep")) {
                                println("Android WebView: Ignoring 404 for /whep endpoint (normal for MediaMTX)")
                                return
                            }
                            
                            // Игнорируем 404 для favicon.ico - это нормально, браузер всегда запрашивает его
                            if (statusCode == 404 && (url.contains("/favicon.ico") || url.endsWith("favicon.ico"))) {
                                println("Android WebView: Ignoring 404 for favicon.ico (normal browser request)")
                                return
                            }
                            
                            // Для других ошибок показываем сообщение только если это не второстепенные ресурсы
                            if (statusCode >= 400 && !url.contains("/whep") && !url.contains("/favicon.ico")) {
                                hasError = true
                                errorMessage = "HTTP ошибка $statusCode"
                                onError?.invoke(errorMessage ?: "Неизвестная ошибка")
                            }
                        }
                    }
                    
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                            println("Android WebView Console: ${consoleMessage?.message()}")
                            return true
                        }
                        
                        // Обработка запросов разрешений для WebRTC (не нужны для просмотра потока)
                        override fun onPermissionRequest(request: android.webkit.PermissionRequest?) {
                            println("Android WebView: Permission request: ${request?.resources?.joinToString()}")
                            // Для просмотра WebRTC потока разрешения не требуются
                            // Но на всякий случай отклоняем запрос корректно
                            request?.deny()
                        }
                        
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            if (newProgress == 100) {
                                println("Android WebView: Page fully loaded")
                            }
                        }
                    }
                    
                    // Формируем URL для MediaMTX
                    // MediaMTX автоматически обрабатывает параметры через основной URL
                    // MediaMTX требует muted=true для автозапуска (стандартное поведение браузеров)
                    val mediaMtxUrl = buildString {
                        append(url)
                        if (!url.endsWith("/")) {
                            append("/")
                        }
                        // Параметры добавляем только для основного URL, MediaMTX сам их обработает
                        append("?autoplay=")
                        append(if (autoPlay) "true" else "false")
                        // Всегда используем muted=true для MediaMTX, иначе видео не запускается
                        append("&muted=true")
                        append("&controls=true")
                        append("&playsinline=true")
                    }
                    
                    println("Android WebView: Loading MediaMTX URL: $mediaMtxUrl")
                    loadUrl(mediaMtxUrl)
                }
            },
            modifier = modifier,
            update = { webView ->
                // Обновляем URL если изменился
                val mediaMtxUrl = buildString {
                    append(url)
                    if (!url.endsWith("/")) {
                        append("/")
                    }
                    append("?autoplay=")
                    append(if (autoPlay) "true" else "false")
                    // Всегда используем muted=true для MediaMTX
                    append("&muted=true")
                    append("&controls=true")
                    append("&playsinline=true")
                }
                if (webView.url != mediaMtxUrl) {
                    webView.loadUrl(mediaMtxUrl)
                }
            }
        )
    } else {
        // Используем ExoPlayer для обычных видео и HLS
        AndroidView(
        factory = { context ->
            val player = ExoPlayer.Builder(context).build()
            
            // Пробуем разные форматы: сначала HLS, потом обычный поток
            // URL может быть WebRTC/HLS endpoint, поэтому пробуем добавить .m3u8
            val mediaItem = if (url.endsWith(".m3u8") || url.contains("/hls/")) {
                // Это HLS поток
                MediaItem.fromUri(Uri.parse(url))
            } else {
                // Пробуем как обычный поток, но с указанием типа для HLS
                // Если это WebRTC endpoint, ExoPlayer не сможет его воспроизвести напрямую
                // В этом случае нужно использовать WebView с HTML video
                MediaItem.Builder()
                    .setUri(Uri.parse(url))
                    .setMimeType("application/x-mpegURL") // Пробуем как HLS
                    .build()
            }
            
            player.setMediaItem(mediaItem)
            player.prepare()
            
            if (autoPlay) {
                player.playWhenReady = true
            }
            
            if (muted) {
                player.volume = 0f
            }
            
            player.addListener(object : Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    // Если ExoPlayer не может воспроизвести, это WebRTC endpoint
                    // В этом случае нужно использовать WebView
                    hasError = true
                    errorMessage = "Формат не поддерживается. Используется WebRTC поток, требуется WebView."
                    onError?.invoke(errorMessage ?: "Неизвестная ошибка")
                }
            })
            
            PlayerView(context).apply {
                this.player = player
                useController = true
            }
        },
        modifier = modifier,
        update = { playerView ->
            val player = playerView.player as? ExoPlayer
            if (player != null) {
                val currentMediaItem = player.currentMediaItem
                val currentUri = currentMediaItem?.localConfiguration?.uri?.toString()
                
                if (currentUri != url) {
                    val mediaItem = if (url.endsWith(".m3u8") || url.contains("/hls/")) {
                        MediaItem.fromUri(Uri.parse(url))
                    } else {
                        MediaItem.Builder()
                            .setUri(Uri.parse(url))
                            .setMimeType("application/x-mpegURL")
                            .build()
                    }
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    
                    if (autoPlay) {
                        player.playWhenReady = true
                    }
                    
                    if (muted) {
                        player.volume = 0f
                    }
                }
            }
        },
        onRelease = { playerView ->
            val player = playerView.player as? ExoPlayer
            player?.release()
        }
        )
    }

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
