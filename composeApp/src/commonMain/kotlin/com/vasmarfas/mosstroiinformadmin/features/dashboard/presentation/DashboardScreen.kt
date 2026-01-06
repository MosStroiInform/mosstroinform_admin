package com.vasmarfas.mosstroiinformadmin.features.dashboard.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vasmarfas.mosstroiinformadmin.core.data.models.Chat
import com.vasmarfas.mosstroiinformadmin.core.data.models.Document
import com.vasmarfas.mosstroiinformadmin.core.data.models.Project
import com.vasmarfas.mosstroiinformadmin.core.theme.AdminTheme
import com.vasmarfas.mosstroiinformadmin.core.ui.*
import com.vasmarfas.mosstroiinformadmin.core.ui.components.*
import com.vasmarfas.mosstroiinformadmin.features.dashboard.presentation.DashboardStats
import kotlinx.datetime.Clock
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreenContent(
    onStatisticsClick: () -> Unit = {},
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    when (val state = uiState) {
        is DashboardUiState.Loading -> {
            LoadingIndicator(text = "Загрузка...")
        }
        is DashboardUiState.Error -> {
            ErrorView(
                message = state.message,
                onRetry = { viewModel.loadDashboard() }
            )
        }
        is DashboardUiState.Success -> {
            DashboardContent(
                stats = state.stats,
                recentProjects = state.recentProjects,
                pendingDocuments = state.pendingDocuments,
                recentChats = state.recentChats
            )
        }
    }
}

@Composable
private fun DashboardContent(
    stats: DashboardStats,
    recentProjects: List<Project>,
    pendingDocuments: List<Document>,
    recentChats: List<Chat>
) {
    val isCompact = isCompactScreen()
    val gridColumns = adaptiveValue(compact = 2, medium = 3, expanded = 4)
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(AdaptivePadding.medium),
        verticalArrangement = Arrangement.spacedBy(AdaptivePadding.medium)
    ) {
        // Заголовок
        item {
            Text(
                text = "Панель управления",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // Статистика
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                horizontalArrangement = Arrangement.spacedBy(AdaptivePadding.small),
                verticalArrangement = Arrangement.spacedBy(AdaptivePadding.small),
                modifier = Modifier.height((120 * if (isCompact) 3 else 2).dp)
            ) {
                item {
                    StatCard(
                        title = "Всего проектов",
                        value = stats.totalProjects.toString(),
                        icon = Icons.Default.Home,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                item {
                    StatCard(
                        title = "Доступные",
                        value = stats.availableProjects.toString(),
                        icon = Icons.Default.CheckCircle,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                item {
                    StatCard(
                        title = "Строительство",
                        value = stats.constructionProjects.toString(),
                        icon = Icons.Default.Build,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                item {
                    StatCard(
                        title = "Документы",
                        value = stats.pendingDocuments.toString(),
                        icon = Icons.Default.Description,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                item {
                    StatCard(
                        title = "Сообщения",
                        value = stats.unreadMessages.toString(),
                        icon = Icons.AutoMirrored.Filled.Chat,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                item {
                    StatCard(
                        title = "Запросы",
                        value = stats.requestedProjects.toString(),
                        icon = Icons.Default.Pending,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
        
        // Разделы
        if (pendingDocuments.isNotEmpty()) {
            item {
                SectionHeader("Документы на рассмотрении")
            }
            items(pendingDocuments) { document ->
                DocumentListItem(document)
            }
        }
        
        if (recentChats.isNotEmpty()) {
            item {
                SectionHeader("Последние сообщения")
            }
            items(recentChats) { chat ->
                ChatListItem(chat)
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AdaptivePadding.medium),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Иконка и цифра в одной строке
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = color
                )
            }
            // Текст под ними
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun DocumentListItem(document: Document) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AdaptivePadding.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = document.description ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusBadge(status = document.status)
        }
    }
}

@Composable
private fun ChatListItem(chat: Chat) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AdaptivePadding.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chat.specialistName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = chat.lastMessage ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            if (chat.unreadCount > 0) {
                Badge {
                    Text(chat.unreadCount.toString())
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StatCardPreview() {
    AdminTheme(darkTheme = true) {
        StatCard(
            title = "Всего проектов",
            value = "42",
            icon = Icons.Default.Home,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DocumentListItemPreview() {
    AdminTheme(darkTheme = true) {
        DocumentListItem(
            document = Document(
                id = "1",
                projectId = "proj1",
                title = "Проектная документация",
                description = "Описание документа",
                status = "pending"
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatListItemPreview() {
    AdminTheme(darkTheme = true) {
        ChatListItem(
            chat = Chat(
                id = "1",
                projectId = "proj1",
                specialistName = "Иван Иванов",
                lastMessage = "Привет, как дела?",
                lastMessageAt = Clock.System.now(),
                unreadCount = 3
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    AdminTheme(darkTheme = true) {
        DashboardContent(
            stats = DashboardStats(
                totalProjects = 42,
                availableProjects = 15,
                constructionProjects = 12,
                pendingDocuments = 8,
                unreadMessages = 5,
                requestedProjects = 7
            ),
            recentProjects = listOf(
                Project(
                    id = "1",
                    name = "Жилой комплекс 'Солнечный'",
                    address = "г. Москва, ул. Ленина, д. 1",
                    description = "Современный жилой комплекс",
                    area = 150.0,
                    floors = 5,
                    price = 50000000,
                    bedrooms = 3,
                    status = "available"
                ),
                Project(
                    id = "2",
                    name = "Жилой комплекс 'Весенний'",
                    address = "г. Москва, ул. Пушкина, д. 10",
                    description = "Новый проект",
                    area = 200.0,
                    floors = 7,
                    price = 75000000,
                    bedrooms = 4,
                    status = "construction"
                )
            ),
            pendingDocuments = listOf(
                Document(
                    id = "1",
                    projectId = "proj1",
                    title = "Проектная документация",
                    description = "Требует рассмотрения",
                    status = "pending"
                ),
                Document(
                    id = "2",
                    projectId = "proj2",
                    title = "Разрешение на строительство",
                    description = "На согласовании",
                    status = "pending"
                )
            ),
            recentChats = listOf(
                Chat(
                    id = "1",
                    projectId = "proj1",
                    specialistName = "Иван Иванов",
                    lastMessage = "Привет, как дела?",
                    lastMessageAt = Clock.System.now(),
                    unreadCount = 3
                ),
                Chat(
                    id = "2",
                    projectId = "proj2",
                    specialistName = "Мария Сидорова",
                    lastMessage = "Все готово",
                    lastMessageAt = Clock.System.now(),
                    unreadCount = 0
                )
            )
        )
    }
}

