package com.vasmarfas.mosstroiinformadmin.features.documents.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vasmarfas.mosstroiinformadmin.core.data.models.Document
import com.vasmarfas.mosstroiinformadmin.core.data.models.DocumentStatus
import com.vasmarfas.mosstroiinformadmin.core.theme.AdminTheme
import com.vasmarfas.mosstroiinformadmin.core.ui.components.EmptyState
import com.vasmarfas.mosstroiinformadmin.core.ui.components.ErrorView
import com.vasmarfas.mosstroiinformadmin.core.ui.components.LoadingIndicator
import com.vasmarfas.mosstroiinformadmin.core.ui.components.StatusBadge
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsListScreen(
    onDocumentClick: (String) -> Unit,
    viewModel: DocumentsListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Фильтры
        FilterChips(
            selectedFilter = state.selectedFilter,
            onFilterSelected = viewModel::setFilter
        )

        // Контент
        PullToRefreshBox(
            isRefreshing = state.isLoading && state.filteredDocuments.isNotEmpty(),
            onRefresh = { viewModel.loadDocuments() },
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                state.isLoading && state.filteredDocuments.isEmpty() -> LoadingIndicator()
                state.error != null && state.filteredDocuments.isEmpty() -> ErrorView(
                    message = state.error ?: "Ошибка загрузки",
                    onRetry = viewModel::loadDocuments
                )
                state.filteredDocuments.isEmpty() -> EmptyState(
                    message = if (state.selectedFilter != null) {
                        "Нет документов с данным фильтром"
                    } else {
                        "Нет документов"
                    },
                    icon = Icons.Default.Description
                )
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.filteredDocuments,
                            key = { it.id }
                        ) { document ->
                            DocumentCard(
                                document = document,
                                onClick = { onDocumentClick(document.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChips(
    selectedFilter: DocumentStatus?,
    onFilterSelected: (DocumentStatus?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { onFilterSelected(null) },
                label = { Text("Все") },
                leadingIcon = if (selectedFilter == null) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }
        
        items(DocumentStatus.entries) { status ->
            FilterChip(
                selected = selectedFilter == status,
                onClick = { onFilterSelected(status) },
                label = { Text(status.displayName) },
                leadingIcon = if (selectedFilter == status) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}

@Composable
private fun DocumentCard(
    document: Document,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(8.dp))
                StatusBadge(status = DocumentStatus.fromValue(document.status).displayName)
            }

            // Описание
            if (!document.description.isNullOrBlank()) {
                Text(
                    text = document.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Информация
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ID проекта
                InfoChip(
                    icon = Icons.Default.Home,
                    text = "Проект: ${document.projectId.take(8)}"
                )

                // Дата подачи
                if (document.submittedAt != null) {
                    InfoChip(
                        icon = Icons.Default.CalendarToday,
                        text = formatDate(document.submittedAt)
                    )
                }
            }

            // Причина отклонения
            if (document.status == "rejected" && !document.rejectionReason.isNullOrBlank()) {
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Причина: ${document.rejectionReason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDate(dateString: String): String {
    // Простое форматирование даты
    // TODO: использовать kotlinx-datetime для правильного форматирования
    return try {
        dateString.take(10)
    } catch (e: Exception) {
        dateString
    }
}

@Preview(showBackground = true)
@Composable
private fun DocumentCardPreview() {
    AdminTheme(darkTheme = true) {
        DocumentCard(
            document = Document(
                id = "1",
                projectId = "proj123",
                title = "Проектная документация",
                description = "Полный комплект проектной документации",
                status = "pending",
                submittedAt = "2024-01-15T10:00:00Z"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DocumentCardRejectedPreview() {
    AdminTheme(darkTheme = true) {
        DocumentCard(
            document = Document(
                id = "2",
                projectId = "proj456",
                title = "Разрешение на строительство",
                description = "Документ требует доработки",
                status = "rejected",
                submittedAt = "2024-01-10T10:00:00Z",
                rejectionReason = "Недостаточно информации"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterChipsPreview() {
    AdminTheme(darkTheme = true) {
        FilterChips(
            selectedFilter = DocumentStatus.PENDING,
            onFilterSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DocumentsListScreenPreview() {
    AdminTheme(darkTheme = true) {
        DocumentsListScreenContentPreview()
    }
}

@Composable
private fun DocumentsListScreenContentPreview() {
    val mockDocuments = listOf(
        Document(
            id = "1",
            projectId = "proj123",
            title = "Проектная документация",
            description = "Полный комплект проектной документации",
            status = "pending",
            submittedAt = "2024-01-15T10:00:00Z"
        ),
        Document(
            id = "2",
            projectId = "proj456",
            title = "Разрешение на строительство",
            description = "Документ требует доработки",
            status = "rejected",
            submittedAt = "2024-01-10T10:00:00Z",
            rejectionReason = "Недостаточно информации"
        ),
        Document(
            id = "3",
            projectId = "proj789",
            title = "Акт выполненных работ",
            description = "Акт по первому этапу",
            status = "approved",
            submittedAt = "2024-01-20T10:00:00Z"
        )
    )
    
    Column(modifier = Modifier.fillMaxSize()) {
        FilterChips(
            selectedFilter = null,
            onFilterSelected = {}
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = mockDocuments,
                key = { it.id }
            ) { document ->
                DocumentCard(
                    document = document,
                    onClick = {}
                )
            }
        }
    }
}

