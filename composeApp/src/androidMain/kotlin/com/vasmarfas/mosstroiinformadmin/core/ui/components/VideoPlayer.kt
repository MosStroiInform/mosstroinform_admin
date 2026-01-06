package com.vasmarfas.mosstroiinformadmin.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/**
 * Реализация VideoPlayer для Android
 * Использует ExoPlayer для RTSP, HLS и обычных видео потоков
 * RTSP поддерживается напрямую через ExoPlayer - это самый простой и надежный способ
 */
@Composable
actual fun VideoPlayer(
    url: String,
    modifier: Modifier,
    autoPlay: Boolean,
    muted: Boolean,
    onError: ((String) -> Unit)?
) {
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(url) {
        hasError = false
        errorMessage = null
    }

    // ExoPlayer поддерживает RTSP, HLS и обычные видео напрямую
    AndroidView(
        factory = { context ->
            val player = ExoPlayer.Builder(context).build()
            
            // ExoPlayer автоматически определяет тип потока по URL
            // Поддерживает: RTSP, HLS (.m3u8), HTTP/HTTPS видео
            val mediaItem = MediaItem.fromUri(Uri.parse(url))
            
            player.setMediaItem(mediaItem)
            player.prepare()
            
            if (autoPlay) {
                player.playWhenReady = true
            }
            
            if (muted) {
                player.volume = 0f
            } else {
                player.volume = 1f
            }
            
            player.addListener(object : Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    hasError = true
                    errorMessage = "Ошибка воспроизведения: ${error.message}"
                    println("ExoPlayer error: ${error.message}")
                    onError?.invoke(errorMessage ?: "Неизвестная ошибка")
                }
                
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            println("ExoPlayer: Ready, playing: ${player.isPlaying}")
                            hasError = false
                        }
                        Player.STATE_BUFFERING -> {
                            println("ExoPlayer: Buffering...")
                        }
                        Player.STATE_ENDED -> {
                            println("ExoPlayer: Ended")
                        }
                        Player.STATE_IDLE -> {
                            println("ExoPlayer: Idle")
                        }
                    }
                }
                
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    println("ExoPlayer: isPlaying = $isPlaying")
                }
            })
            
            PlayerView(context).apply {
                this.player = player
                useController = true
                // Включаем автоматическое масштабирование
                resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            }
        },
        modifier = modifier,
        update = { playerView ->
            val player = playerView.player as? ExoPlayer
            if (player != null) {
                val currentMediaItem = player.currentMediaItem
                val currentUri = currentMediaItem?.localConfiguration?.uri?.toString()
                
                // Обновляем URL если изменился
                if (currentUri != url) {
                    println("ExoPlayer: Updating URL from $currentUri to $url")
                    val mediaItem = MediaItem.fromUri(Uri.parse(url))
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    
                    if (autoPlay) {
                        player.playWhenReady = true
                    }
                    
                    if (muted) {
                        player.volume = 0f
                    } else {
                        player.volume = 1f
                    }
                }
                
                // Обновляем состояние воспроизведения
                if (autoPlay && !player.isPlaying && player.playbackState == Player.STATE_READY) {
                    player.play()
                }
            }
        },
        onRelease = { playerView ->
            val player = playerView.player as? ExoPlayer
            player?.release()
            println("ExoPlayer: Released")
        }
    )

    // Показываем ошибку если есть
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
}
