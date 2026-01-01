package com.vasmarfas.mosstroiinformadmin.features.chats.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vasmarfas.mosstroiinformadmin.core.data.models.Chat
import com.vasmarfas.mosstroiinformadmin.core.theme.AdminTheme
import com.vasmarfas.mosstroiinformadmin.core.ui.components.EmptyState
import com.vasmarfas.mosstroiinformadmin.core.ui.components.ErrorView
import com.vasmarfas.mosstroiinformadmin.core.ui.components.LoadingIndicator
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    onChatClick: (String) -> Unit,
    viewModel: ChatListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()
    
    PullToRefreshBox(
        isRefreshing = state.isLoading && state.chats.isNotEmpty(),
        onRefresh = { viewModel.refreshChats() },
        state = pullToRefreshState,
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            state.isLoading && state.chats.isEmpty() -> {
                LoadingIndicator()
            }
            state.error != null && state.chats.isEmpty() -> {
                ErrorView(
                    message = state.error ?: "Неизвестная ошибка",
                    onRetry = { viewModel.refreshChats() }
                )
            }
            state.chats.isEmpty() -> {
                EmptyState(
                    message = "Нет активных чатов",
                    subtitle = "Чаты появятся после начала диалога со специалистом"
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = state.chats,
                        key = { it.id }
                    ) { chat ->
                        ChatListItem(
                            chat = chat,
                            onClick = { onChatClick(chat.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatListItem(
    chat: Chat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватар
            com.vasmarfas.mosstroiinformadmin.core.ui.components.AvatarImage(
                imageUrl = chat.specialistAvatarUrl,
                name = chat.specialistName,
                size = 48.dp
            )
            
            // Контент
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.specialistName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (chat.lastMessageAt != null) {
                        Text(
                            text = formatTime(chat.lastMessageAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (chat.lastMessage != null) {
                    Text(
                        text = chat.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (chat.unreadCount > 0) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Badge с количеством непрочитанных
            if (chat.unreadCount > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = chat.unreadCount.toString(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

private fun formatTime(instant: Instant): String {
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.hour.toString().padStart(2, '0')}:${localDateTime.minute.toString().padStart(2, '0')}"
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
                specialistAvatarUrl = null,
                lastMessage = "Привет! Как дела с проектом?",
                lastMessageAt = Clock.System.now(),
                unreadCount = 5
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatListItemNoUnreadPreview() {
    AdminTheme(darkTheme = true) {
        ChatListItem(
            chat = Chat(
                id = "2",
                projectId = "proj2",
                specialistName = "Мария Сидорова",
                specialistAvatarUrl = null,
                lastMessage = "Все готово, можно приступать",
                lastMessageAt = Clock.System.now(),
                unreadCount = 0
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatListScreenPreview() {
    AdminTheme(darkTheme = true) {
        ChatListScreenContentPreview()
    }
}

@Composable
private fun ChatListScreenContentPreview() {
    val mockChats = listOf(
        Chat(
            id = "1",
            projectId = "proj1",
            specialistName = "Иван Иванов",
            specialistAvatarUrl = null,
            lastMessage = "Привет! Как дела с проектом?",
            lastMessageAt = Clock.System.now(),
            unreadCount = 5
        ),
        Chat(
            id = "2",
            projectId = "proj2",
            specialistName = "Мария Сидорова",
            specialistAvatarUrl = null,
            lastMessage = "Все готово, можно приступать",
            lastMessageAt = Clock.System.now(),
            unreadCount = 0
        ),
        Chat(
            id = "3",
            projectId = "proj3",
            specialistName = "Петр Петров",
            specialistAvatarUrl = null,
            lastMessage = "Нужна консультация по документам",
            lastMessageAt = Clock.System.now(),
            unreadCount = 2
        )
    )
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = mockChats,
            key = { it.id }
        ) { chat ->
            ChatListItem(
                chat = chat,
                onClick = {}
            )
        }
    }
}

