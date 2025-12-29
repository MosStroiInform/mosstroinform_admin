package com.vasmarfas.mosstroiinformadmin.features.documents.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vasmarfas.mosstroiinformadmin.core.data.models.DocumentStatus
import com.vasmarfas.mosstroiinformadmin.core.ui.components.LoadingIndicator
import com.vasmarfas.mosstroiinformadmin.core.ui.components.StatusBadge
import io.ktor.client.HttpClient
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailDialog(
    documentId: String,
    onDismiss: () -> Unit,
    viewModel: DocumentDetailViewModel = koinInject { parametersOf(documentId) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showRejectDialog by remember { mutableStateOf(false) }

    // Автоматически закрыть диалог при успехе
    LaunchedEffect(state.actionSuccess) {
        if (state.actionSuccess) {
            kotlinx.coroutines.delay(1500)
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Заголовок
                TopAppBar(
                    title = { Text("Детали документа") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Закрыть")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator()
                        }
                    }
                    state.error != null && state.document == null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
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
                                text = state.error ?: "Ошибка загрузки",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = viewModel::loadDocument) {
                                Text("Повторить")
                            }
                        }
                    }
                    state.document != null -> {
                        val document = state.document!!
                        val status = DocumentStatus.fromValue(document.status)

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Статус
                            StatusBadge(status = status.displayName)

                            // Название
                            Text(
                                text = document.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            HorizontalDivider()

                            // Описание
                            if (!document.description.isNullOrBlank()) {
                                DetailRow(
                                    label = "Описание",
                                    value = document.description
                                )
                            }

                            // ID проекта
                            DetailRow(
                                label = "ID проекта",
                                value = document.projectId
                            )

                            // Дата подачи
                            if (document.submittedAt != null) {
                                DetailRow(
                                    label = "Дата подачи",
                                    value = document.submittedAt
                                )
                            }

                            // Дата одобрения
                            if (document.approvedAt != null) {
                                DetailRow(
                                    label = "Дата одобрения",
                                    value = document.approvedAt
                                )
                            }

                            // Причина отклонения
                            if (!document.rejectionReason.isNullOrBlank()) {
                                HorizontalDivider()
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Error,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                            Text(
                                                text = "Причина отклонения",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                        Text(
                                            text = document.rejectionReason,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }

                            // Ссылка на файл
                            if (!document.fileUrl.isNullOrBlank()) {
                                HorizontalDivider()
                                var isDownloading by remember { mutableStateOf(false) }
                                var downloadError by remember { mutableStateOf<String?>(null) }
                                val httpClient: HttpClient = koinInject()
                                val scope = rememberCoroutineScope()
                                
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            isDownloading = true
                                            downloadError = null
                                            scope.launch {
                                                val fileName = document.fileUrl.substringAfterLast('/')
                                                val result = com.vasmarfas.mosstroiinformadmin.core.utils.downloadAndOpenFile(
                                                    url = document.fileUrl,
                                                    fileName = fileName,
                                                    httpClient = httpClient
                                                )
                                                isDownloading = false
                                                if (result.isFailure) {
                                                    downloadError = result.exceptionOrNull()?.message ?: "Ошибка скачивания"
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !isDownloading
                                    ) {
                                        if (isDownloading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(Icons.Default.Download, contentDescription = null)
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text(if (isDownloading) "Скачивание..." else "Скачать документ")
                                    }
                                    
                                    if (downloadError != null) {
                                        Text(
                                            text = "Ошибка: $downloadError",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            // Сообщение об успехе
                            if (state.actionSuccess) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            "Действие выполнено успешно",
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }

                        // Кнопки действий
                        if (status == DocumentStatus.PENDING || status == DocumentStatus.UNDER_REVIEW) {
                            HorizontalDivider()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Кнопка отклонения
                                OutlinedButton(
                                    onClick = { showRejectDialog = true },
                                    modifier = Modifier.weight(1f),
                                    enabled = !state.isApproving && !state.isRejecting,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    if (state.isRejecting) {
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

                                // Кнопка одобрения
                                Button(
                                    onClick = viewModel::approveDocument,
                                    modifier = Modifier.weight(1f),
                                    enabled = !state.isApproving && !state.isRejecting
                                ) {
                                    if (state.isApproving) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Одобрить")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Диалог отклонения
    if (showRejectDialog) {
        RejectDialog(
            onDismiss = { showRejectDialog = false },
            onConfirm = { reason ->
                viewModel.rejectDocument(reason)
                showRejectDialog = false
            }
        )
    }
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

