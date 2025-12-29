package com.vasmarfas.mosstroiinformadmin.features.admin.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectCreateEditScreen(
    projectId: String? = null,
    onBackClick: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: ProjectCreateEditViewModel = koinViewModel(parameters = { parametersOf(projectId) })
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var floors by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var bedrooms by remember { mutableStateOf("0") }
    var bathrooms by remember { mutableStateOf("0") }
    var imageUrl by remember { mutableStateOf("") }
    var stagesText by remember { mutableStateOf("") }

    // Автоматически закрыть при успехе
    LaunchedEffect(state.success) {
        if (state.success) {
            kotlinx.coroutines.delay(1500)
            onSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (projectId == null) "Создать проект" else "Редактировать проект") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
                    }
                },
                actions = {
                    if (projectId != null) {
                        IconButton(
                            onClick = {
                                viewModel.deleteProject(onSuccess)
                            },
                            enabled = !state.isSaving
                        ) {
                            Icon(Icons.Default.Delete, "Удалить", tint = MaterialTheme.colorScheme.error)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Сообщение об успехе
            if (state.success) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            if (projectId == null) "Проект создан успешно" else "Проект обновлен успешно",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Ошибка
            if (state.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            state.error ?: "Ошибка",
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Поля формы
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Адрес *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = area,
                    onValueChange = { area = it },
                    label = { Text("Площадь (м²) *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = floors,
                    onValueChange = { floors = it },
                    label = { Text("Этажи *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Цена (₽) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = bedrooms,
                    onValueChange = { bedrooms = it },
                    label = { Text("Спальни") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = bathrooms,
                    onValueChange = { bathrooms = it },
                    label = { Text("Ванные") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("URL изображения") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = stagesText,
                onValueChange = { stagesText = it },
                label = { Text("Этапы (через запятую)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Фундамент, Стены, Крыша") },
                minLines = 2,
                maxLines = 4
            )

            // Кнопка сохранения
            Button(
                onClick = {
                    val stages = stagesText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    
                    if (projectId == null) {
                        viewModel.createProject(
                            name = name,
                            address = address,
                            description = description,
                            area = area.toFloatOrNull() ?: 0f,
                            floors = floors.toIntOrNull() ?: 0,
                            price = price.toFloatOrNull() ?: 0f,
                            bedrooms = bedrooms.toIntOrNull() ?: 0,
                            bathrooms = bathrooms.toIntOrNull() ?: 0,
                            imageUrl = imageUrl.ifBlank { null },
                            stages = stages
                        )
                    } else {
                        viewModel.updateProject(
                            name = name.ifBlank { null },
                            address = address.ifBlank { null },
                            description = description.ifBlank { null },
                            area = area.toFloatOrNull(),
                            floors = floors.toIntOrNull(),
                            price = price.toFloatOrNull(),
                            bedrooms = bedrooms.toIntOrNull(),
                            bathrooms = bathrooms.toIntOrNull(),
                            imageUrl = imageUrl.ifBlank { null }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving && name.isNotBlank() && address.isNotBlank() && 
                         area.toFloatOrNull() != null && floors.toIntOrNull() != null && 
                         price.toFloatOrNull() != null
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (projectId == null) "Создать" else "Сохранить")
                }
            }
        }
    }
}

