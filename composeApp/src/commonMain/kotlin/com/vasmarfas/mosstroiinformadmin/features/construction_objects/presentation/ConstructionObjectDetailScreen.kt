package com.vasmarfas.mosstroiinformadmin.features.construction_objects.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vasmarfas.mosstroiinformadmin.core.theme.AdminTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.vasmarfas.mosstroiinformadmin.core.data.models.ConstructionObject
import com.vasmarfas.mosstroiinformadmin.core.data.models.ConstructionObjectStage
import com.vasmarfas.mosstroiinformadmin.core.data.models.StageStatus
import com.vasmarfas.mosstroiinformadmin.core.ui.components.ErrorView
import com.vasmarfas.mosstroiinformadmin.core.ui.components.LoadingIndicator
import com.vasmarfas.mosstroiinformadmin.core.ui.components.StatusBadge
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConstructionObjectDetailScreen(
    objectId: String,
    onBackClick: () -> Unit,
    viewModel: ConstructionObjectDetailViewModel = koinViewModel { parametersOf(objectId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showDocumentsStatusDialog by remember { mutableStateOf(false) }

    // Автоочистка успеха
    LaunchedEffect(state.actionSuccess) {
        if (state.actionSuccess) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearActionSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.constructionObject?.name ?: "Объект") },
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
                state.error != null && state.constructionObject == null -> ErrorView(
                    message = state.error ?: "Ошибка загрузки",
                    onRetry = viewModel::loadObject
                )
                state.constructionObject != null -> {
                    ObjectContent(
                        obj = state.constructionObject!!,
                        isCompleting = state.isCompleting,
                        isUpdatingDocumentsStatus = state.isUpdatingDocumentsStatus,
                        updatingStageId = state.updatingStageId,
                        onComplete = { showCompleteDialog = true },
                        onUpdateDocumentsStatus = { showDocumentsStatusDialog = true },
                        onUpdateStageStatus = viewModel::updateStageStatus
                    )
                }
            }

            // Snackbar успеха
            if (state.actionSuccess) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text("Действие выполнено успешно")
                }
            }
        }
    }

    // Диалог завершения
    if (showCompleteDialog) {
        CompleteObjectDialog(
            objectName = state.constructionObject?.name ?: "",
            isCompleted = state.constructionObject?.isCompleted ?: false,
            allDocumentsSigned = state.constructionObject?.allDocumentsSigned ?: false,
            onDismiss = { showCompleteDialog = false },
            onConfirm = {
                viewModel.completeObject()
                showCompleteDialog = false
            }
        )
    }

    // Диалог обновления статуса документов
    if (showDocumentsStatusDialog) {
        DocumentsStatusDialog(
            currentStatus = state.constructionObject?.allDocumentsSigned ?: false,
            onDismiss = { showDocumentsStatusDialog = false },
            onConfirm = { allSigned ->
                viewModel.updateDocumentsStatus(allSigned)
                showDocumentsStatusDialog = false
            }
        )
    }
}

@Composable
private fun ObjectContent(
    obj: ConstructionObject,
    isCompleting: Boolean,
    isUpdatingDocumentsStatus: Boolean,
    updatingStageId: String?,
    onComplete: () -> Unit,
    onUpdateDocumentsStatus: () -> Unit,
    onUpdateStageStatus: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Информационная карточка
        item {
            ObjectInfoCard(obj = obj)
        }

        // Этапы
        if (obj.stages.isNotEmpty()) {
            item {
                Text(
                    text = "Этапы строительства",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(
                items = obj.stages,
                key = { it.id }
            ) { stage ->
                StageCard(
                    stage = stage,
                    isUpdating = updatingStageId == stage.id,
                    onUpdateStatus = { status -> onUpdateStageStatus(stage.id, status) }
                )
            }
        }

        // Действия
        if (!obj.isCompleted) {
            item {
                ActionsCard(
                    obj = obj,
                    isCompleting = isCompleting,
                    isUpdatingDocumentsStatus = isUpdatingDocumentsStatus,
                    onComplete = onComplete,
                    onUpdateDocumentsStatus = onUpdateDocumentsStatus
                )
            }
        }
    }
}

@Composable
private fun ObjectInfoCard(obj: ConstructionObject) {
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
            // Статус
            if (obj.isCompleted) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "Завершен",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Название
            Text(
                text = obj.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // Описание
            if (obj.description.isNotBlank()) {
                Text(
                    text = obj.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            // Адрес
            InfoRow(
                icon = Icons.Default.LocationOn,
                label = "Адрес",
                value = obj.address
            )

            // Характеристики
            InfoRow(
                icon = Icons.Default.Home,
                label = "Площадь",
                value = "${obj.area.toInt()} м²"
            )

            InfoRow(
                icon = Icons.Default.Build,
                label = "Этажей",
                value = obj.floors.toString()
            )

            if (obj.bedrooms > 0) {
                InfoRow(
                    icon = Icons.Default.Person,
                    label = "Спален",
                    value = obj.bedrooms.toString()
                )
            }

            if (obj.bathrooms > 0) {
                InfoRow(
                    icon = Icons.Default.Star,
                    label = "Ванных",
                    value = obj.bathrooms.toString()
                )
            }

            HorizontalDivider()

            // Статус документов
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (obj.allDocumentsSigned) Icons.Default.CheckCircle else Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (obj.allDocumentsSigned) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = "Документы",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = if (obj.allDocumentsSigned) "Все подписаны" else "Ожидают подписи",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (obj.allDocumentsSigned) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            HorizontalDivider()

            // Цена
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Стоимость:",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${formatPrice(obj.price)} ₽",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StageCard(
    stage: ConstructionObjectStage,
    isUpdating: Boolean = false,
    onUpdateStatus: (String) -> Unit = {}
) {
    val status = StageStatus.fromValue(stage.status)
    val color = when (status) {
        StageStatus.COMPLETED -> MaterialTheme.colorScheme.primary
        StageStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiary
        StageStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icon = when (status) {
                    StageStatus.COMPLETED -> Icons.Default.CheckCircle
                    StageStatus.IN_PROGRESS -> Icons.Default.Build
                    StageStatus.PENDING -> Icons.Default.Star
                }

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stage.name,
                        style = MaterialTheme.typography.titleSmall
                    )
                    StatusBadge(status = status.displayName)
                }
            }

            // Кнопки для изменения статуса
            if (status != StageStatus.COMPLETED) {
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (status) {
                        StageStatus.PENDING -> {
                            Button(
                                onClick = { onUpdateStatus("in_progress") },
                                modifier = Modifier.weight(1f),
                                enabled = !isUpdating
                            ) {
                                if (isUpdating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Начать")
                                }
                            }
                        }
                        StageStatus.IN_PROGRESS -> {
                            Button(
                                onClick = { onUpdateStatus("completed") },
                                modifier = Modifier.weight(1f),
                                enabled = !isUpdating
                            ) {
                                if (isUpdating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Завершить")
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionsCard(
    obj: ConstructionObject,
    isCompleting: Boolean,
    isUpdatingDocumentsStatus: Boolean,
    onComplete: () -> Unit,
    onUpdateDocumentsStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Действия",
                style = MaterialTheme.typography.titleMedium
            )

            // Обновление статуса документов
            Button(
                onClick = onUpdateDocumentsStatus,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCompleting && !isUpdatingDocumentsStatus
            ) {
                if (isUpdatingDocumentsStatus) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = if (obj.allDocumentsSigned) Icons.Default.Edit else Icons.Default.CheckCircle,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (obj.allDocumentsSigned) "Изменить статус документов" else "Отметить документы как подписанные")
                }
            }

            // Завершение объекта
            Button(
                onClick = onComplete,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCompleting && !isUpdatingDocumentsStatus && obj.allDocumentsSigned,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isCompleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Завершить объект")
                }
            }

            if (!obj.allDocumentsSigned) {
                Text(
                    text = "Для завершения объекта все документы должны быть подписаны",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CompleteObjectDialog(
    objectName: String,
    isCompleted: Boolean,
    allDocumentsSigned: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Завершить объект") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Вы уверены, что хотите завершить объект \"$objectName\"?")
                if (!allDocumentsSigned) {
                    Text(
                        text = "⚠️ Все документы должны быть подписаны",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (isCompleted) {
                    Text(
                        text = "Объект уже завершен",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = allDocumentsSigned && !isCompleted
            ) {
                Text("Завершить")
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
private fun DocumentsStatusDialog(
    currentStatus: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    var newStatus by remember { mutableStateOf(currentStatus) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Статус документов") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Изменить статус подписания документов:")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = newStatus,
                        onClick = { newStatus = true }
                    )
                    Text("Все документы подписаны")
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !newStatus,
                        onClick = { newStatus = false }
                    )
                    Text("Документы ожидают подписи")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(newStatus) },
                enabled = newStatus != currentStatus
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

private fun formatPrice(price: Int): String {
    return price.toString().reversed().chunked(3).joinToString(" ").reversed()
}

@Preview(showBackground = true)
@Composable
private fun ObjectInfoCardPreview() {
    AdminTheme(darkTheme = true) {
        ObjectInfoCard(
            obj = ConstructionObject(
                id = "1",
                projectId = "proj1",
                name = "Жилой комплекс 'Солнечный'",
                address = "г. Москва, ул. Ленина, д. 1",
                description = "Современный жилой комплекс",
                area = 150.0f,
                floors = 5,
                bedrooms = 3,
                bathrooms = 2,
                price = 50000000,
                isCompleted = false,
                allDocumentsSigned = true
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StageCardPreview() {
    AdminTheme(darkTheme = true) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StageCard(
                stage = ConstructionObjectStage("1", "Фундамент", "completed")
            )
            StageCard(
                stage = ConstructionObjectStage("2", "Стены", "in_progress")
            )
            StageCard(
                stage = ConstructionObjectStage("3", "Кровля", "pending")
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoRowPreview() {
    AdminTheme(darkTheme = true) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoRow(
                icon = Icons.Default.Home,
                label = "Площадь",
                value = "150 м²"
            )
            InfoRow(
                icon = Icons.Default.Build,
                label = "Этажей",
                value = "5"
            )
            InfoRow(
                icon = Icons.Default.Person,
                label = "Спален",
                value = "3"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConstructionObjectDetailScreenPreview() {
    AdminTheme(darkTheme = true) {
        ObjectContent(
            obj = ConstructionObject(
                id = "1",
                projectId = "proj1",
                name = "Жилой комплекс 'Солнечный'",
                address = "г. Москва, ул. Ленина, д. 1",
                description = "Современный жилой комплекс",
                area = 150.0f,
                floors = 5,
                bedrooms = 3,
                bathrooms = 2,
                price = 50000000,
                isCompleted = false,
                allDocumentsSigned = true,
                stages = listOf(
                    ConstructionObjectStage("1", "Фундамент", "completed"),
                    ConstructionObjectStage("2", "Стены", "in_progress"),
                    ConstructionObjectStage("3", "Кровля", "pending")
                )
            ),
            isCompleting = false,
            isUpdatingDocumentsStatus = false,
            updatingStageId = null,
            onComplete = {},
            onUpdateDocumentsStatus = {},
            onUpdateStageStatus = { _, _ -> }
        )
    }
}

