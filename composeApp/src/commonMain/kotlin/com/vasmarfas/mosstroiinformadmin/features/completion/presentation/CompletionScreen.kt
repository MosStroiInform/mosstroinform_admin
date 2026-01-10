package com.vasmarfas.mosstroiinformadmin.features.completion.presentation

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
import kotlinx.datetime.Clock
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.vasmarfas.mosstroiinformadmin.core.data.models.CompletionStatus
import com.vasmarfas.mosstroiinformadmin.core.data.models.FinalDocument
import com.vasmarfas.mosstroiinformadmin.core.data.models.FinalDocumentStatus
import com.vasmarfas.mosstroiinformadmin.core.ui.components.ErrorView
import com.vasmarfas.mosstroiinformadmin.core.ui.components.LoadingIndicator
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletionScreen(
    projectId: String,
    onBackClick: () -> Unit
) {
    // Используем key для принудительного пересоздания ViewModel при изменении projectId
    key(projectId) {
        val viewModel: CompletionViewModel = koinViewModel(
            key = "completion_$projectId",
            parameters = { parametersOf(projectId) }
        )
        
        CompletionScreenContent(
            projectId = projectId,
            viewModel = viewModel,
            onBackClick = onBackClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompletionScreenContent(
    projectId: String,
    viewModel: CompletionViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showRejectDialog by remember { mutableStateOf<String?>(null) }
    var showCreateDocumentDialog by remember { mutableStateOf(false) }
    
    // Перезагружаем данные при изменении projectId
    LaunchedEffect(projectId) {
        viewModel.updateProjectId(projectId)
    }

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
                title = { Text("Завершение проекта") },
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
                state.error != null && state.completionStatus == null -> ErrorView(
                    message = state.error ?: "Ошибка загрузки",
                    onRetry = viewModel::loadCompletionStatus
                )
                state.completionStatus != null -> {
                    CompletionContent(
                        completionStatus = state.completionStatus!!,
                        actionInProgress = state.actionInProgress,
                        isCompleting = state.isCompleting,
                        isCreatingDocument = state.isCreatingDocument,
                        onSignDocument = viewModel::signDocument,
                        onRejectDocument = { documentId ->
                            showRejectDialog = documentId
                        },
                        onCreateDocument = { showCreateDocumentDialog = true },
                        onCompleteProject = viewModel::completeProject
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

    // Диалог отклонения
    if (showRejectDialog != null) {
        RejectDialog(
            onDismiss = { showRejectDialog = null },
            onConfirm = { reason ->
                viewModel.rejectDocument(showRejectDialog!!, reason)
                showRejectDialog = null
            }
        )
    }

    // Диалог создания документа
    if (showCreateDocumentDialog) {
        CreateFinalDocumentDialog(
            onDismiss = { showCreateDocumentDialog = false },
            onConfirm = { title, description, fileUrl ->
                viewModel.createFinalDocument(title, description, fileUrl)
                showCreateDocumentDialog = false
            }
        )
    }
}

@Composable
private fun CompletionContent(
    completionStatus: CompletionStatus,
    actionInProgress: String?,
    isCompleting: Boolean,
    isCreatingDocument: Boolean,
    onSignDocument: (String) -> Unit,
    onRejectDocument: (String) -> Unit,
    onCreateDocument: () -> Unit,
    onCompleteProject: () -> Unit
) {
    // Кнопка завершения проекта
    // Показываем если проект еще не завершен, прогресс 100%, есть документы и все они подписаны
    val allDocumentsSigned = completionStatus.documents.isNotEmpty() && 
        completionStatus.documents.all { it.status == "signed" }
    
    val canComplete = !completionStatus.isCompleted && 
        completionStatus.progress >= 1.0f &&
        completionStatus.documents.isNotEmpty() &&
        allDocumentsSigned
    
        // Отладочная информация (используем стабильный ключ)
        LaunchedEffect(
            completionStatus.isCompleted,
            completionStatus.progress,
            completionStatus.documents.size,
            allDocumentsSigned
        ) {
            println("=== Условия завершения проекта ===")
            println("isCompleted: ${completionStatus.isCompleted}")
            println("progress: ${completionStatus.progress} (>= 1.0f: ${completionStatus.progress >= 1.0f})")
            println("documents.isNotEmpty(): ${completionStatus.documents.isNotEmpty()}")
            println("allDocumentsSigned (флаг): ${completionStatus.allDocumentsSigned}")
            println("all documents signed (проверка): $allDocumentsSigned")
            println("Статусы документов: ${completionStatus.documents.map { "${it.title}: ${it.status}" }}")
            println("canComplete: $canComplete")
        }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Статус завершения
        item {
            StatusCard(completionStatus = completionStatus)
        }

        // Прогресс
        item {
            ProgressCard(progress = completionStatus.progress)
        }

        // Индикатор "Все документы подписаны"
        // Показываем только если есть документы и все они подписаны
        if (completionStatus.documents.isNotEmpty() && 
            completionStatus.allDocumentsSigned &&
            completionStatus.documents.all { it.status == "signed" }) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Все документы подписаны!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Финальные документы
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Финальные документы (${completionStatus.documents.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onCreateDocument,
                    enabled = !isCreatingDocument
                ) {
                    if (isCreatingDocument) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Добавить документ"
                        )
                    }
                }
            }
        }

        if (completionStatus.documents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Финальные документы еще не загружены",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Нажмите кнопку + выше, чтобы создать новый финальный документ.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(
                items = completionStatus.documents,
                key = { it.id }
            ) { document ->
                FinalDocumentCard(
                    document = document,
                    isActionInProgress = actionInProgress == document.id,
                    onSign = { onSignDocument(document.id) },
                    onReject = { onRejectDocument(document.id) }
                )
            }
        }

        if (canComplete) {
            item {
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
                            text = "Завершение проекта",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Все условия выполнены. Вы можете завершить проект.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onCompleteProject,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isCompleting
                        ) {
                            if (isCompleting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.CheckCircle, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Завершить проект")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusCard(completionStatus: CompletionStatus) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (completionStatus.isCompleted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Статус проекта",
                    style = MaterialTheme.typography.titleMedium
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (completionStatus.isCompleted) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.tertiary
                    }
                ) {
                    Text(
                        text = if (completionStatus.isCompleted) "Завершен" else "В процессе",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (completionStatus.isCompleted) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onTertiary
                        }
                    )
                }
            }

            if (completionStatus.completionDate != null) {
                HorizontalDivider()
                val localDate = completionStatus.completionDate.toLocalDateTime(TimeZone.currentSystemDefault())
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Дата завершения: ${localDate.dayOfMonth.toString().padStart(2, '0')}.${localDate.monthNumber.toString().padStart(2, '0')}.${localDate.year}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressCard(progress: Float) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Общий прогресс",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun FinalDocumentCard(
    document: FinalDocument,
    isActionInProgress: Boolean,
    onSign: () -> Unit,
    onReject: () -> Unit
) {
    val status = FinalDocumentStatus.fromValue(document.status)

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
            // Заголовок и статус
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (status) {
                        FinalDocumentStatus.PENDING -> MaterialTheme.colorScheme.tertiaryContainer
                        FinalDocumentStatus.SIGNED -> MaterialTheme.colorScheme.primaryContainer
                        FinalDocumentStatus.REJECTED -> MaterialTheme.colorScheme.errorContainer
                    }
                ) {
                    Text(
                        text = status.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (status) {
                            FinalDocumentStatus.PENDING -> MaterialTheme.colorScheme.onTertiaryContainer
                            FinalDocumentStatus.SIGNED -> MaterialTheme.colorScheme.onPrimaryContainer
                            FinalDocumentStatus.REJECTED -> MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }

            // Описание
            if (document.description.isNotBlank()) {
                Text(
                    text = document.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Даты
            document.submittedAt?.let {
                val localDate = it.toLocalDateTime(TimeZone.currentSystemDefault())
                Text(
                    text = "Подан: ${localDate.dayOfMonth.toString().padStart(2, '0')}.${localDate.monthNumber.toString().padStart(2, '0')}.${localDate.year}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            document.signedAt?.let {
                val localDate = it.toLocalDateTime(TimeZone.currentSystemDefault())
                Text(
                    text = "Подписан: ${localDate.dayOfMonth.toString().padStart(2, '0')}.${localDate.monthNumber.toString().padStart(2, '0')}.${localDate.year}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Кнопки действий (только для pending)
            if (status == FinalDocumentStatus.PENDING) {
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        enabled = !isActionInProgress,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        if (isActionInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Отклонить")
                        }
                    }

                    Button(
                        onClick = onSign,
                        modifier = Modifier.weight(1f),
                        enabled = !isActionInProgress
                    ) {
                        if (isActionInProgress) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Подписать")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RejectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Отклонить документ") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Укажите причину отклонения:")
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Причина") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason) },
                enabled = reason.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Отклонить")
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
private fun CreateFinalDocumentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var fileUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Создать финальный документ") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Название документа *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Описание") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                OutlinedTextField(
                    value = fileUrl,
                    onValueChange = { fileUrl = it },
                    label = { Text("URL файла (опционально)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("https://example.com/document.pdf") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, description, fileUrl.ifBlank { null }) },
                enabled = title.isNotBlank()
            ) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun StatusCardPreview() {
    AdminTheme(darkTheme = true) {
        StatusCard(
            completionStatus = CompletionStatus(
                projectId = "1",
                isCompleted = true,
                completionDate = Clock.System.now(),
                progress = 1.0f,
                documents = emptyList()
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProgressCardPreview() {
    AdminTheme(darkTheme = true) {
        ProgressCard(progress = 0.75f)
    }
}

@Preview(showBackground = true)
@Composable
private fun FinalDocumentCardPreview() {
    AdminTheme(darkTheme = true) {
        FinalDocumentCard(
            document = FinalDocument(
                id = "1",
                title = "Акт приёмки",
                description = "Акт приёмки выполненных работ",
                status = "pending",
                submittedAt = Clock.System.now()
            ),
            isActionInProgress = false,
            onSign = {},
            onReject = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CompletionScreenPreview() {
    AdminTheme(darkTheme = true) {
        CompletionContent(
            completionStatus = CompletionStatus(
                projectId = "1",
                isCompleted = false,
                completionDate = null,
                progress = 0.75f,
                allDocumentsSigned = false,
                documents = listOf(
                    FinalDocument(
                        id = "1",
                        title = "Акт приёмки",
                        description = "Акт приёмки выполненных работ",
                        status = "pending",
                        submittedAt = Clock.System.now()
                    ),
                    FinalDocument(
                        id = "2",
                        title = "Акт выполненных работ",
                        description = "Акт по первому этапу",
                        status = "signed",
                        submittedAt = Clock.System.now(),
                        signedAt = Clock.System.now()
                    )
                )
            ),
            actionInProgress = null,
            isCompleting = false,
            isCreatingDocument = false,
            onSignDocument = {},
            onRejectDocument = {},
            onCreateDocument = {},
            onCompleteProject = {}
        )
    }
}

