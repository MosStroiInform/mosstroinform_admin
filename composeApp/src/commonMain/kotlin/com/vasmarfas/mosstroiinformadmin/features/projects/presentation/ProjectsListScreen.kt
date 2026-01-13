package com.vasmarfas.mosstroiinformadmin.features.projects.presentation

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
import com.vasmarfas.mosstroiinformadmin.core.data.models.Project
import com.vasmarfas.mosstroiinformadmin.core.data.models.ProjectStatus
import com.vasmarfas.mosstroiinformadmin.core.data.models.ProjectStage
import com.vasmarfas.mosstroiinformadmin.core.theme.AdminTheme
import com.vasmarfas.mosstroiinformadmin.core.ui.components.EmptyState
import com.vasmarfas.mosstroiinformadmin.core.ui.components.ErrorView
import com.vasmarfas.mosstroiinformadmin.core.ui.components.LoadingIndicator
import com.vasmarfas.mosstroiinformadmin.core.ui.components.StatusBadge
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsListScreen(
    onProjectClick: (String) -> Unit,
    onCreateProject: () -> Unit = {},
    onEditProject: (String) -> Unit = {},
    onApproveRequest: ((String) -> Unit)? = null,
    onRejectRequest: ((String) -> Unit)? = null,
    viewModel: ProjectsListViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pullToRefreshState = rememberPullToRefreshState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = state.isLoading && state.projects.isNotEmpty(),
            onRefresh = { viewModel.refreshProjects() },
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                state.isLoading && state.projects.isEmpty() -> {
                    LoadingIndicator()
                }
                state.error != null && state.projects.isEmpty() -> {
                    ErrorView(
                        message = state.error ?: "Ошибка загрузки",
                        onRetry = { viewModel.refreshProjects() }
                    )
                }
                state.projects.isEmpty() -> {
                    EmptyState(
                        message = "Нет проектов",
                        subtitle = "Проекты появятся после добавления"
                    )
                }
                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Фильтры
                        FilterChips(
                            selectedFilter = state.selectedFilter,
                            onFilterSelected = { viewModel.setFilter(it) },
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
                        )
                        
                        // Карточки проектов
                        if (state.filteredProjects.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                EmptyState(
                                    message = "Нет проектов с выбранным фильтром",
                                    subtitle = "Попробуйте изменить фильтр"
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(
                                    items = state.filteredProjects,
                                    key = { it.id }
                                ) { project ->
                                    ProjectCard(
                                        project = project,
                                        isCompleted = state.completedProjects[project.id] ?: false,
                                        onClick = { onProjectClick(project.id) },
                                        onApproveRequest = if (project.status == "requested") {
                                            { onApproveRequest?.invoke(project.id) }
                                        } else null,
                                        onRejectRequest = if (project.status == "requested") {
                                            { onRejectRequest?.invoke(project.id) }
                                        } else null
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // FAB для создания проекта
        FloatingActionButton(
            onClick = onCreateProject,
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, "Создать проект")
        }
    }
}

@Composable
private fun FilterChips(
    selectedFilter: ProjectStatus?,
    onFilterSelected: (ProjectStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { onFilterSelected(null) },
                label = { Text("Все") }
            )
        }
        items(ProjectStatus.entries) { status ->
            FilterChip(
                selected = selectedFilter == status,
                onClick = { onFilterSelected(status) },
                label = { Text(
                    text = status.displayName
                ) }
            )
        }
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    isCompleted: Boolean = false,
    onClick: () -> Unit,
    onApproveRequest: ((String) -> Unit)? = null,
    onRejectRequest: ((String) -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                // Показываем "Завершен" если isCompleted = true, иначе используем статус проекта
                val statusText = if (isCompleted) {
                    "Завершен"
                } else {
                    ProjectStatus.fromValue(project.status).displayName
                }
                StatusBadge(status = statusText)
            }
            
            // Address
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = project.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProjectDetail(
                    icon = Icons.Default.Home,
                    text = "${project.area.toInt()} м²"
                )
                ProjectDetail(
                    icon = Icons.Default.Build,
                    text = "${project.floors} эт."
                )
                if (project.bedrooms > 0) {
                    ProjectDetail(
                        icon = Icons.Default.Person,
                        text = "${project.bedrooms} спал."
                    )
                }
            }
            
            // Price
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Стоимость:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${formatPrice(project.price)} ₽",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Stages progress
            if (project.stages.isNotEmpty()) {
                val completedStages = project.stages.count { it.status == "completed" }
                val totalStages = project.stages.size
                
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Прогресс:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$completedStages / $totalStages этапов",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    LinearProgressIndicator(
                        progress = { if (totalStages > 0) completedStages.toFloat() / totalStages else 0f },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Кнопки управления запросами
//            if (project.status == "requested") {
//                HorizontalDivider(modifier = Modifier.fillMaxWidth())
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    OutlinedButton(
//                        onClick = { onRejectRequest?.invoke(project.id) },
//                        modifier = Modifier.weight(1f),
//                        colors = ButtonDefaults.outlinedButtonColors(
//                            contentColor = MaterialTheme.colorScheme.error
//                        )
//                    ) {
//                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
//                        Spacer(Modifier.width(4.dp))
//                        Text("Отклонить")
//                    }
//                    Button(
//                        onClick = { onApproveRequest?.invoke(project.id) },
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
//                        Spacer(Modifier.width(4.dp))
//                        Text("Одобрить")
//                    }
//                }
//            }
        }
    }
}

@Composable
private fun ProjectDetail(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatPrice(price: Int): String {
    return price.toString().reversed().chunked(3).joinToString(" ").reversed()
}

@Preview(showBackground = true)
@Composable
private fun ProjectCardPreview() {
    AdminTheme(darkTheme = true) {
        ProjectCard(
            project = Project(
                id = "1",
                name = "Жилой комплекс 'Солнечный'",
                address = "г. Москва, ул. Ленина, д. 1",
                description = "Современный жилой комплекс",
                area = 150.0,
                floors = 5,
                price = 50000000,
                bedrooms = 3,
                status = "available",
                stages = listOf(
                    ProjectStage("1", "Фундамент", "completed"),
                    ProjectStage("2", "Стены", "in_progress"),
                    ProjectStage("3", "Кровля", "pending")
                )
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProjectCardRequestedPreview() {
    AdminTheme(darkTheme = true) {
        ProjectCard(
            project = Project(
                id = "2",
                name = "Жилой комплекс 'Весенний'",
                address = "г. Москва, ул. Пушкина, д. 10",
                description = "Новый проект",
                area = 200.0,
                floors = 7,
                price = 75000000,
                bedrooms = 4,
                status = "requested",
                stages = emptyList()
            ),
            onClick = {},
            onApproveRequest = {},
            onRejectRequest = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterChipsPreview() {
    AdminTheme(darkTheme = true) {
        FilterChips(
            selectedFilter = ProjectStatus.AVAILABLE,
            onFilterSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProjectsListScreenPreview() {
    AdminTheme(darkTheme = true) {
        ProjectsListScreenContentPreview()
    }
}

@Composable
private fun ProjectsListScreenContentPreview() {
    val mockProjects = listOf(
        Project(
            id = "1",
            name = "Жилой комплекс 'Солнечный'",
            address = "г. Москва, ул. Ленина, д. 1",
            description = "Современный жилой комплекс",
            area = 150.0,
            floors = 5,
            price = 50000000,
            bedrooms = 3,
            status = "available",
            stages = listOf(
                ProjectStage("1", "Фундамент", "completed"),
                ProjectStage("2", "Стены", "in_progress"),
                ProjectStage("3", "Кровля", "pending")
            )
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
            status = "requested",
            stages = emptyList()
        ),
        Project(
            id = "3",
            name = "Жилой комплекс 'Летний'",
            address = "г. Москва, ул. Гагарина, д. 5",
            description = "Элитный комплекс",
            area = 180.0,
            floors = 6,
            price = 90000000,
            bedrooms = 5,
            status = "construction",
            stages = listOf(
                ProjectStage("1", "Фундамент", "completed"),
                ProjectStage("2", "Стены", "completed"),
                ProjectStage("3", "Кровля", "in_progress")
            )
        )
    )
    
    Column(modifier = Modifier.fillMaxSize()) {
        FilterChips(
            selectedFilter = null,
            onFilterSelected = {}
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = mockProjects,
                key = { it.id }
            ) { project ->
                ProjectCard(
                    project = project,
                    onClick = {}
                )
            }
        }
    }
}

