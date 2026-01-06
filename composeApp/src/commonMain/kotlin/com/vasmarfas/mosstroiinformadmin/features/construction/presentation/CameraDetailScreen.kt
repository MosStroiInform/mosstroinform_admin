package com.vasmarfas.mosstroiinformadmin.features.construction.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.vasmarfas.mosstroiinformadmin.core.ui.components.loadImageFromUrl
import com.vasmarfas.mosstroiinformadmin.core.ui.components.VideoPlayer
import io.ktor.client.HttpClient
import org.koin.compose.koinInject
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vasmarfas.mosstroiinformadmin.core.theme.AdminTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.vasmarfas.mosstroiinformadmin.core.data.models.Camera
import com.vasmarfas.mosstroiinformadmin.core.ui.components.ErrorView
import com.vasmarfas.mosstroiinformadmin.core.ui.components.LoadingIndicator
import com.vasmarfas.mosstroiinformadmin.core.utils.openUrlInBrowser
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraDetailScreen(
    siteId: String,
    cameraId: String,
    onBackClick: () -> Unit,
    viewModel: CameraDetailViewModel = koinViewModel { parametersOf(siteId, cameraId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    // Обновляем siteId и cameraId в ViewModel при изменении
    LaunchedEffect(siteId, cameraId) {
        viewModel.updateCameraId(siteId, cameraId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.camera?.name ?: "Камера") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> LoadingIndicator()
                state.error != null && state.camera == null -> ErrorView(
                    message = state.error ?: "Ошибка загрузки",
                    onRetry = viewModel::loadCamera
                )
                state.camera != null -> {
                    CameraContent(camera = state.camera!!, httpClient = org.koin.compose.koinInject())
                }
            }
        }
    }
}

@Composable
private fun CameraContent(camera: Camera, httpClient: HttpClient = koinInject()) {
    var showSettingsDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Видео превью или поток
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                    // Преобразуем RTSP в HTTPS для веб-просмотра (WebRTC через MediaMTX)
                    val videoUrl = remember(camera.streamUrl) {
                        if (camera.streamUrl.startsWith("rtsp://")) {
                            // Преобразуем rtsp://mosstroiinformmedia.vasmarfas.com:8554/vid1
                            // в https://mosstroiinformmedia.vasmarfas.com:8889/vid1/
                            // MediaMTX предоставляет WebRTC через HTTPS на порту 8889
                            var url = camera.streamUrl
                                .replace("rtsp://", "https://")
                                .replace(":8554/", ":8889/")
                                .replace(":8554", ":8889")
                            // Убеждаемся, что URL заканчивается на / для MediaMTX
                            if (!url.endsWith("/")) {
                                url += "/"
                            }
                            url
                        } else {
                            camera.streamUrl
                        }
                    }
                
                // Показываем видеопоток если камера активна
                when {
                    camera.isActive -> {
                        // Показываем видеопоток
                        VideoPlayer(
                            url = videoUrl,
                            modifier = Modifier.fillMaxSize(),
                            autoPlay = true,
                            muted = true,  // MediaMTX требует muted=true для автозапуска
                            onError = { error ->
                                println("Video player error: $error")
                            }
                        )
                    }
                    camera.thumbnailUrl != null && camera.thumbnailUrl.isNotBlank() -> {
                        // Показываем превью изображение
                        var imageBitmap by remember(camera.thumbnailUrl) { mutableStateOf<ImageBitmap?>(null) }
                        var isLoading by remember(camera.thumbnailUrl) { mutableStateOf(true) }
                        var loadError by remember(camera.thumbnailUrl) { mutableStateOf<String?>(null) }
                        
                        LaunchedEffect(camera.thumbnailUrl) {
                            isLoading = true
                            loadError = null
                            try {
                                println("Loading thumbnail from: ${camera.thumbnailUrl}")
                                imageBitmap = loadImageFromUrl(camera.thumbnailUrl, httpClient)
                                if (imageBitmap == null) {
                                    println("Failed to load thumbnail: imageBitmap is null")
                                    loadError = "Не удалось загрузить изображение"
                                } else {
                                    println("Thumbnail loaded successfully")
                                }
                            } catch (e: Exception) {
                                // Игнорируем LeftCompositionCancellationException - это нормально при выходе из композиции
                                val isCancellation = e::class.simpleName?.contains("LeftComposition") == true || 
                                                     e::class.simpleName?.contains("Cancellation") == true
                                if (!isCancellation) {
                                    println("Error loading thumbnail: ${e.message}")
                                    loadError = e.message ?: "Ошибка загрузки"
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                        
                        when {
                            isLoading -> {
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                            imageBitmap != null -> {
                                Image(
                                    bitmap = imageBitmap!!,
                                    contentDescription = camera.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            loadError != null -> {
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
                                            imageVector = Icons.Default.BrokenImage,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            text = "Ошибка загрузки",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            else -> {
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
                                            imageVector = Icons.Default.Videocam,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        // Показываем видеопоток или кнопку для открытия
                        // Для веб-версии можно встроить iframe, для нативных - кнопку
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Пробуем показать iframe для веб-версии (только для HTTPS)
                                if (videoUrl.startsWith("https://")) {
                                    // Для веб-версии можно использовать HTML iframe
                                    // Но в Compose Multiplatform это сложно, поэтому показываем кнопку
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Видеопоток доступен",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            // Открываем URL в браузере
                                            openUrlInBrowser(videoUrl)
                                        }
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Открыть видеопоток")
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = "Видеопоток",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "URL: $videoUrl",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Индикатор LIVE
                if (camera.isActive) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.error
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    modifier = Modifier.size(10.dp),
                                    shape = MaterialTheme.shapes.extraSmall,
                                    color = MaterialTheme.colorScheme.onError
                                ) {}
                            }
                            Text(
                                text = "LIVE",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onError,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Информация о камере
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Информация",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider()

                DetailRow(label = "Название", value = camera.name)

                if (camera.description.isNotBlank()) {
                    DetailRow(label = "Описание", value = camera.description)
                }

                DetailRow(
                    label = "Статус",
                    value = if (camera.isActive) "Активна" else "Неактивна"
                )
            }
        }

        // Техническая информация
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Техническая информация",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                HorizontalDivider()

                DetailRow(label = "Stream URL", value = camera.streamUrl)

                if (camera.thumbnailUrl != null) {
                    DetailRow(label = "Thumbnail URL", value = camera.thumbnailUrl)
                }
            }
        }

        // Инструкция
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Информация",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "Для просмотра видеопотока в реальном времени используйте мобильное приложение. " +
                            "Админ-панель показывает информацию о камере и позволяет управлять настройками.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Кнопки действий
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { 
                    // Обновляем камеру
                    // TODO: добавить refresh в ViewModel
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Обновить")
            }

            Button(
                onClick = { showSettingsDialog = true },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Настройки")
            }
        }
        
        // Диалог настроек
        if (showSettingsDialog) {
            CameraSettingsDialog(
                camera = camera,
                onDismiss = { showSettingsDialog = false },
                onSave = { updatedCamera ->
                    // TODO: сохранить изменения через ViewModel
                    showSettingsDialog = false
                }
            )
        }
    }
}

@Composable
private fun CameraSettingsDialog(
    camera: Camera,
    onDismiss: () -> Unit,
    onSave: (Camera) -> Unit
) {
    var name by remember { mutableStateOf(camera.name) }
    var description by remember { mutableStateOf(camera.description) }
    var streamUrl by remember { mutableStateOf(camera.streamUrl) }
    var thumbnailUrl by remember { mutableStateOf(camera.thumbnailUrl ?: "") }
    var isActive by remember { mutableStateOf(camera.isActive) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройки камеры") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                OutlinedTextField(
                    value = streamUrl,
                    onValueChange = { streamUrl = it },
                    label = { Text("Stream URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = thumbnailUrl,
                    onValueChange = { thumbnailUrl = it },
                    label = { Text("Thumbnail URL") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Активна")
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        camera.copy(
                            name = name,
                            description = description,
                            streamUrl = streamUrl,
                            thumbnailUrl = thumbnailUrl.ifBlank { null },
                            isActive = isActive
                        )
                    )
                }
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailRowPreview() {
    AdminTheme(darkTheme = true) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            DetailRow(
                label = "Название",
                value = "Камера 1"
            )
            DetailRow(
                label = "Описание",
                value = "Основная камера на площадке"
            )
            DetailRow(
                label = "Статус",
                value = "Активна"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CameraDetailScreenPreview() {
    AdminTheme(darkTheme = true) {
        CameraContent(
            camera = Camera(
                id = "1",
                name = "Камера 1",
                description = "Основная камера на строительной площадке",
                streamUrl = "rtsp://example.com/stream",
                isActive = true,
                thumbnailUrl = null
            ),
            httpClient = org.koin.compose.koinInject()
        )
    }
}

