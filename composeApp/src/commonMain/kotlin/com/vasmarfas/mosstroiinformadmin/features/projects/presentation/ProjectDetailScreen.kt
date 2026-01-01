package com.vasmarfas.mosstroiinformadmin.features.projects.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vasmarfas.mosstroiinformadmin.core.data.models.Project
import com.vasmarfas.mosstroiinformadmin.core.data.models.ProjectStage
import com.vasmarfas.mosstroiinformadmin.core.data.models.ProjectStatus
import com.vasmarfas.mosstroiinformadmin.core.data.models.StageStatus
import com.vasmarfas.mosstroiinformadmin.core.theme.AdminTheme
import com.vasmarfas.mosstroiinformadmin.core.ui.components.ErrorView
import com.vasmarfas.mosstroiinformadmin.core.ui.components.LoadingIndicator
import com.vasmarfas.mosstroiinformadmin.core.ui.components.StatusBadge
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: String,
    onBackClick: () -> Unit,
    onViewConstructionSite: () -> Unit = {},
    onViewCompletion: () -> Unit = {},
    onEditProject: ((String) -> Unit)? = null
) {
    // Используем key для принудительного пересоздания ViewModel при изменении projectId
    key(projectId) {
        val viewModel: ProjectDetailViewModel = koinViewModel(
            key = "project_detail_$projectId",
            parameters = { parametersOf(projectId) }
        )
        
        ProjectDetailScreenContent(
            projectId = projectId,
            viewModel = viewModel,
            onBackClick = onBackClick,
            onViewConstructionSite = onViewConstructionSite,
            onViewCompletion = onViewCompletion,
            onEditProject = onEditProject
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectDetailScreenContent(
    projectId: String,
    viewModel: ProjectDetailViewModel,
    onBackClick: () -> Unit,
    onViewConstructionSite: () -> Unit,
    onViewCompletion: () -> Unit,
    onEditProject: ((String) -> Unit)?
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showStartDialog by remember { mutableStateOf(false) }
    
    // Snackbar для успеха
    LaunchedEffect(state.actionSuccess) {
        if (state.actionSuccess) {
            kotlinx.coroutines.delay(2000)
            viewModel.clearActionSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = state.project?.name ?: "Проект",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    if (onEditProject != null) {
                        IconButton(onClick = { onEditProject(projectId) }) {
                            Icon(Icons.Default.Edit, "Редактировать")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> {
                    LoadingIndicator()
                }
                state.error != null && state.project == null -> {
                    ErrorView(
                        message = state.error ?: "Ошибка загрузки",
                        onRetry = { viewModel.refreshProject() }
                    )
                }
                state.project != null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Основная информация
                        item {
                            ProjectInfoCard(project = state.project!!)
                        }
                        
                        // Действия
                        item {
                            ProjectActionsCard(
                                project = state.project!!,
                                isRequestingConstruction = state.isRequestingConstruction,
                                isStartingConstruction = state.isStartingConstruction,
                                onRequestConstruction = { viewModel.requestConstruction() },
                                onStartConstruction = { showStartDialog = true },
                                onViewConstructionSite = onViewConstructionSite,
                                onViewCompletion = onViewCompletion
                            )
                        }
                        
                        // Этапы
                        state.project?.stages?.takeIf { it.isNotEmpty() }?.let { stages ->
                            item {
                                Text(
                                    text = "Этапы строительства",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            
                            items(
                                items = stages,
                                key = { it.id }
                            ) { stage ->
                                StageCard(stage = stage)
                            }
                        }
                    }
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
    
    // Диалог начала строительства
    if (showStartDialog) {
        StartConstructionDialog(
            address = state.project?.address ?: "",
            onDismiss = { showStartDialog = false },
            onConfirm = { address ->
                viewModel.startConstruction(address)
                showStartDialog = false
            }
        )
    }
}

@Composable
private fun ProjectInfoCard(project: Project) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Статус
            StatusBadge(status = ProjectStatus.fromValue(project.status).displayName)
            
            // Название
            Text(
                text = project.name,
                style = MaterialTheme.typography.headlineSmall
            )
            
            // Описание
            if (project.description.isNotBlank()) {
                Text(
                    text = project.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            HorizontalDivider()
            
            // Адрес
            InfoRow(
                icon = Icons.Default.LocationOn,
                label = "Адрес",
                value = project.address
            )
            
            // Площадь
            InfoRow(
                icon = Icons.Default.Home,
                label = "Площадь",
                value = "${project.area.toInt()} м²"
            )
            
            // Этажи
            InfoRow(
                icon = Icons.Default.Build,
                label = "Этажей",
                value = project.floors.toString()
            )
            
            // Спальни
            if (project.bedrooms > 0) {
                InfoRow(
                    icon = Icons.Default.Person,
                    label = "Спален",
                    value = project.bedrooms.toString()
                )
            }
            
            // Ванные
            if (project.bathrooms > 0) {
                InfoRow(
                    icon = Icons.Default.Star,
                    label = "Ванных",
                    value = project.bathrooms.toString()
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
                    text = "${formatPrice(project.price)} ₽",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ProjectActionsCard(
    project: Project,
    isRequestingConstruction: Boolean,
    isStartingConstruction: Boolean,
    onRequestConstruction: () -> Unit,
    onStartConstruction: () -> Unit,
    onViewConstructionSite: () -> Unit,
    onViewCompletion: () -> Unit
) {
    val status = ProjectStatus.fromValue(project.status)
    
    if (status == ProjectStatus.AVAILABLE || status == ProjectStatus.REQUESTED || status == ProjectStatus.IN_PROGRESS || status == ProjectStatus.COMPLETED) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Действия",
                    style = MaterialTheme.typography.titleMedium
                )
                
                when (status) {
                    ProjectStatus.AVAILABLE -> {
                        Button(
                            onClick = onRequestConstruction,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isRequestingConstruction
                        ) {
                            if (isRequestingConstruction) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Запросить строительство")
                            }
                        }
                    }
                    ProjectStatus.REQUESTED -> {
                        Button(
                            onClick = onStartConstruction,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isStartingConstruction
                        ) {
                            if (isStartingConstruction) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Default.Build, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Начать строительство")
                            }
                        }
                    }
                    ProjectStatus.IN_PROGRESS -> {
                        Button(
                            onClick = onViewConstructionSite,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Смотреть строительную площадку")
                        }
                    }
                    ProjectStatus.COMPLETED -> {
                        Button(
                            onClick = onViewCompletion,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Завершение проекта")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StageCard(stage: ProjectStage) {
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Иконка статуса
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
            
            // Информация
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stage.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = status.displayName,
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun StartConstructionDialog(
    address: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var addressText by remember { mutableStateOf(address) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Начать строительство") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Подтвердите адрес строительства:")
                OutlinedTextField(
                    value = addressText,
                    onValueChange = { addressText = it },
                    label = { Text("Адрес") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(addressText) },
                enabled = addressText.isNotBlank()
            ) {
                Text("Начать")
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
private fun StageCardPreview() {
    AdminTheme(darkTheme = true) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            StageCard(
                stage = ProjectStage("1", "Фундамент", "completed")
            )
            StageCard(
                stage = ProjectStage("2", "Стены", "in_progress")
            )
            StageCard(
                stage = ProjectStage("3", "Кровля", "pending")
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
                label = "Этажи",
                value = "5"
            )
            InfoRow(
                icon = Icons.Default.Person,
                label = "Спальни",
                value = "3"
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProjectInfoCardPreview() {
    AdminTheme(darkTheme = true) {
        ProjectInfoCard(
            project = Project(
                id = "1",
                name = "Жилой комплекс 'Солнечный'",
                address = "г. Москва, ул. Ленина, д. 1",
                description = "Современный жилой комплекс с развитой инфраструктурой",
                area = 150.0,
                floors = 5,
                price = 50000000,
                bedrooms = 3,
                bathrooms = 2,
                status = "available"
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProjectActionsCardPreview() {
    AdminTheme(darkTheme = true) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ProjectActionsCard(
                project = Project(
                    id = "1",
                    name = "Проект",
                    address = "Адрес",
                    area = 100.0,
                    floors = 3,
                    price = 30000000,
                    status = "available"
                ),
                isRequestingConstruction = false,
                isStartingConstruction = false,
                onRequestConstruction = {},
                onStartConstruction = {},
                onViewConstructionSite = {},
                onViewCompletion = {}
            )
            ProjectActionsCard(
                project = Project(
                    id = "2",
                    name = "Проект",
                    address = "Адрес",
                    area = 100.0,
                    floors = 3,
                    price = 30000000,
                    status = "requested"
                ),
                isRequestingConstruction = false,
                isStartingConstruction = false,
                onRequestConstruction = {},
                onStartConstruction = {},
                onViewConstructionSite = {},
                onViewCompletion = {}
            )
            ProjectActionsCard(
                project = Project(
                    id = "3",
                    name = "Проект",
                    address = "Адрес",
                    area = 100.0,
                    floors = 3,
                    price = 30000000,
                    status = "construction"
                ),
                isRequestingConstruction = false,
                isStartingConstruction = false,
                onRequestConstruction = {},
                onStartConstruction = {},
                onViewConstructionSite = {},
                onViewCompletion = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProjectDetailScreenPreview() {
    AdminTheme(darkTheme = true) {
        ProjectDetailScreenContentPreview()
    }
}

@Composable
private fun ProjectDetailScreenContentPreview() {
    val mockProject = Project(
        id = "1",
        name = "Жилой комплекс 'Солнечный'",
        address = "г. Москва, ул. Ленина, д. 1",
        description = "Современный жилой комплекс с развитой инфраструктурой",
        area = 150.0,
        floors = 5,
        price = 50000000,
        bedrooms = 3,
        bathrooms = 2,
        status = "available",
        stages = listOf(
            ProjectStage("1", "Фундамент", "completed"),
            ProjectStage("2", "Стены", "in_progress"),
            ProjectStage("3", "Кровля", "pending")
        )
    )
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ProjectInfoCard(project = mockProject)
        }
        item {
            ProjectActionsCard(
                project = mockProject,
                isRequestingConstruction = false,
                isStartingConstruction = false,
                onRequestConstruction = {},
                onStartConstruction = {},
                onViewConstructionSite = {},
                onViewCompletion = {}
            )
        }
        item {
            Text(
                text = "Этапы проекта",
                style = MaterialTheme.typography.titleMedium
            )
        }
        items(mockProject.stages) { stage ->
            StageCard(stage = stage)
        }
    }
}

