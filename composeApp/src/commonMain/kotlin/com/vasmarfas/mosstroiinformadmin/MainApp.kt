package com.vasmarfas.mosstroiinformadmin

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vasmarfas.mosstroiinformadmin.core.theme.AdminTheme
import com.vasmarfas.mosstroiinformadmin.core.ui.isCompactScreen
import com.vasmarfas.mosstroiinformadmin.features.auth.data.AuthRepository
import com.vasmarfas.mosstroiinformadmin.features.auth.presentation.LoginScreen
import com.vasmarfas.mosstroiinformadmin.features.dashboard.presentation.DashboardScreenContent
import com.vasmarfas.mosstroiinformadmin.features.chats.presentation.ChatListScreen
import com.vasmarfas.mosstroiinformadmin.features.chats.presentation.ChatDetailScreen
import com.vasmarfas.mosstroiinformadmin.features.projects.presentation.ProjectsListScreen
import com.vasmarfas.mosstroiinformadmin.features.projects.presentation.ProjectDetailScreen
import com.vasmarfas.mosstroiinformadmin.features.documents.presentation.DocumentsListScreen
import com.vasmarfas.mosstroiinformadmin.features.documents.presentation.DocumentDetailDialog
import com.vasmarfas.mosstroiinformadmin.features.construction.presentation.SiteDetailScreen
import com.vasmarfas.mosstroiinformadmin.features.construction.presentation.CameraDetailScreen
import com.vasmarfas.mosstroiinformadmin.features.construction_objects.presentation.ConstructionObjectsListScreen
import com.vasmarfas.mosstroiinformadmin.features.construction_objects.presentation.ConstructionObjectDetailScreen
import com.vasmarfas.mosstroiinformadmin.features.completion.presentation.CompletionScreen
import com.vasmarfas.mosstroiinformadmin.features.admin.presentation.ProjectCreateEditScreen
import com.vasmarfas.mosstroiinformadmin.features.admin.presentation.StatisticsScreen
import com.vasmarfas.mosstroiinformadmin.features.admin.presentation.RequestManagementViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

enum class Screen {
    LOGIN, MAIN
}

@Composable
fun MainApp() {
    // Принудительно используем тёмную тему для лучшего вида
    AdminTheme(darkTheme = true) {
        // Применяем фон ко всему приложению
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val authRepository: AuthRepository = koinInject()
            val scope = rememberCoroutineScope()
            
            var currentScreen by remember { mutableStateOf<Screen?>(null) }
            
            LaunchedEffect(Unit) {
                // Проверяем авторизацию асинхронно
                val isLoggedIn = authRepository.isLoggedIn()
                currentScreen = if (isLoggedIn) {
                    // Дополнительно проверяем, что токен валидный
                    try {
                        val userResult = authRepository.getMe()
                        if (userResult is com.vasmarfas.mosstroiinformadmin.core.network.ApiResult.Success) {
                            Screen.MAIN
                        } else {
                            // Токен невалидный, очищаем и показываем логин
                            authRepository.logout()
                            Screen.LOGIN
                        }
                    } catch (e: Exception) {
                        // Ошибка при проверке, показываем логин
                        authRepository.logout()
                        Screen.LOGIN
                    }
                } else {
                    Screen.LOGIN
                }
            }
            
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith 
                    fadeOut(animationSpec = tween(300))
                }
            ) { screen ->
                when (screen) {
                    Screen.LOGIN -> {
                        LoginScreen(
                            onLoginSuccess = {
                                currentScreen = Screen.MAIN
                            }
                        )
                    }
                    Screen.MAIN -> {
                        MainScreenContent(
                            onLogout = {
                                scope.launch {
                                    authRepository.logout()
                                    currentScreen = Screen.LOGIN
                                }
                            }
                        )
                    }
                    null -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScreenContent(onLogout: () -> Unit) {
    val isCompact = isCompactScreen()
    var selectedTab by remember { mutableStateOf(0) }
    
    // Уменьшаем до 4 вкладок для мобильного меню + More
    val tabs = if (isCompact) {
        listOf(
            NavigationItem("Главная", Icons.Default.Dashboard),
            NavigationItem("Проекты", Icons.Default.Home),
            NavigationItem("Документы", Icons.Default.Description),
            NavigationItem("Ещё", Icons.Default.MoreHoriz),
        )
    } else {
        listOf(
            NavigationItem("Главная", Icons.Default.Dashboard),
            NavigationItem("Проекты", Icons.Default.Home),
            NavigationItem("Объекты", Icons.Default.Build),
            NavigationItem("Документы", Icons.Default.Description),
            NavigationItem("Чаты", Icons.AutoMirrored.Filled.Chat),
            NavigationItem("Аналитика", Icons.Default.Assessment),
        )
    }
    
    // "More" menu state
    var showMoreMenu by remember { mutableStateOf(false) }
    
    if (isCompact) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = when (selectedTab) {
                                0 -> "Главная"
                                1 -> "Проекты"
                                2 -> "Документы"
                                3 -> "Ещё"
                                4 -> "Объекты"
                                5 -> "Чаты"
                                6 -> "Аналитика"
                                else -> "МосСтройИнформ"
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, "Выход")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    tabs.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { 
                                Text(
                                    text = item.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            selected = if (index == 3) {
                                selectedTab >= 4 // "More" selected when showing extra tabs
                            } else {
                                selectedTab == index
                            },
                            onClick = { 
                                if (index == 3) {
                                    showMoreMenu = true
                                } else {
                                    selectedTab = index
                                }
                            }
                        )
                    }
                }
                
                // Dropdown menu for "More"
                DropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { showMoreMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Объекты") },
                        leadingIcon = { Icon(Icons.Default.Build, null) },
                        onClick = { 
                            selectedTab = 4
                            showMoreMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Чаты") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Chat, null) },
                        onClick = { 
                            selectedTab = 5
                            showMoreMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Аналитика") },
                        leadingIcon = { Icon(Icons.Default.Assessment, null) },
                        onClick = { 
                            selectedTab = 6
                            showMoreMenu = false
                        }
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                TabContent(selectedTab)
            }
        }
    } else {
        // Desktop/Tablet layout с NavigationRail
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                modifier = Modifier.width(80.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Spacer(Modifier.height(16.dp))
                tabs.forEachIndexed { index, item ->
                    NavigationRailItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { 
                            Text(
                                text = item.title,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
                Spacer(Modifier.weight(1f))
                NavigationRailItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, "Выход") },
                    label = { 
                        Text(
                            text = "Выход",
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    selected = false,
                    onClick = onLogout
                )
                Spacer(Modifier.height(16.dp))
            }
            
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            Box(modifier = Modifier.fillMaxSize()) {
                TabContent(selectedTab)
            }
        }
    }
}

@Composable
private fun TabContent(selectedTab: Int) {
    val isCompact = isCompactScreen()
    
    // Используем AnimatedContent для плавных переходов
    AnimatedContent(
        targetState = selectedTab,
        transitionSpec = {
            fadeIn(animationSpec = tween(200)) togetherWith 
            fadeOut(animationSpec = tween(200))
        }
    ) { tab ->
        if (isCompact) {
            // Мобильная версия: 0-Главная, 1-Проекты, 2-Документы, 3-Ещё, 4-Объекты, 5-Чаты, 6-Аналитика
            when (tab) {
                0 -> DashboardScreenContent(
                    onStatisticsClick = { /* Статистика на отдельном табе */ }
                )
                1 -> ProjectsScreenContainer()
                2 -> DocumentsScreenContainer()
                3 -> {} // Placeholder for "More" tab (не должен показываться)
                4 -> ConstructionObjectsScreenContainer()
                5 -> ChatsScreenContainer()
                6 -> StatisticsScreen(onBackClick = { })
                else -> DashboardScreenContent(
                    onStatisticsClick = { /* Статистика на отдельном табе */ }
                )
            }
        } else {
            // Широкая версия: 0-Главная, 1-Проекты, 2-Объекты, 3-Документы, 4-Чаты, 5-Аналитика
            when (tab) {
                0 -> DashboardScreenContent(
                    onStatisticsClick = { /* Статистика на отдельном табе */ }
                )
                1 -> ProjectsScreenContainer()
                2 -> ConstructionObjectsScreenContainer()
                3 -> DocumentsScreenContainer()
                4 -> ChatsScreenContainer()
                5 -> StatisticsScreen(onBackClick = { })
                else -> DashboardScreenContent(
                    onStatisticsClick = { /* Статистика на отдельном табе */ }
                )
            }
        }
    }
}

@Composable
private fun ChatsScreenContainer() {
    var selectedChatId by remember { mutableStateOf<String?>(null) }
    
    AnimatedContent(
        targetState = selectedChatId,
        transitionSpec = {
            if (targetState != null) {
                slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { fullWidth -> fullWidth }
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { fullWidth -> -fullWidth }
                )
            } else {
                slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { fullWidth -> -fullWidth }
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { fullWidth -> fullWidth }
                )
            }
        }
    ) { chatId ->
        if (chatId != null) {
            ChatDetailScreen(
                chatId = chatId,
                onBackClick = { selectedChatId = null }
            )
        } else {
            ChatListScreen(
                onChatClick = { id -> selectedChatId = id }
            )
        }
    }
}

@Composable
private fun ProjectsScreenContainer() {
    var selectedProjectId by remember { mutableStateOf<String?>(null) }
    var showConstructionSite by remember { mutableStateOf(false) }
    var showCompletion by remember { mutableStateOf(false) }
    var selectedCamera by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showCreateProject by remember { mutableStateOf(false) }
    var editProjectId by remember { mutableStateOf<String?>(null) }
    
    val requestViewModel: RequestManagementViewModel = koinViewModel()
    
    // Определяем текущий экран
    val currentScreen = when {
        showCreateProject -> "create"
        editProjectId != null -> "edit"
        selectedCamera != null -> "camera"
        showCompletion && selectedProjectId != null -> "completion"
        showConstructionSite && selectedProjectId != null -> "site"
        selectedProjectId != null -> "detail"
        else -> "list"
    }
    
    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (targetState == "list") {
                slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { -it }
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { it }
                )
            } else {
                slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { it }
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { -it }
                )
            }
        }
    ) { screen ->
        when (screen) {
            "create" -> {
                ProjectCreateEditScreen(
                    projectId = null,
                    onBackClick = { showCreateProject = false },
                    onSuccess = { showCreateProject = false }
                )
            }
            "edit" -> {
                ProjectCreateEditScreen(
                    projectId = editProjectId,
                    onBackClick = { editProjectId = null },
                    onSuccess = { editProjectId = null }
                )
            }
            "camera" -> {
                selectedCamera?.let { camera ->
                    CameraDetailScreen(
                        siteId = camera.first,
                        cameraId = camera.second,
                        onBackClick = { selectedCamera = null }
                    )
                }
            }
            "completion" -> {
                selectedProjectId?.let { projectId ->
                    CompletionScreen(
                        projectId = projectId,
                        onBackClick = { showCompletion = false }
                    )
                }
            }
            "site" -> {
                selectedProjectId?.let { projectId ->
                    SiteDetailScreen(
                        projectId = projectId,
                        onBackClick = { showConstructionSite = false },
                        onCameraClick = { siteId, cameraId ->
                            selectedCamera = siteId to cameraId
                        }
                    )
                }
            }
            "detail" -> {
                selectedProjectId?.let { projectId ->
                    ProjectDetailScreen(
                        projectId = projectId,
                        onBackClick = { selectedProjectId = null },
                        onViewConstructionSite = { showConstructionSite = true },
                        onViewCompletion = { showCompletion = true },
                        onEditProject = { editProjectId = it }
                    )
                }
            }
            else -> {
                ProjectsListScreen(
                    onProjectClick = { projectId -> selectedProjectId = projectId },
                    onCreateProject = { showCreateProject = true },
                    onEditProject = { editProjectId = it },
                    onApproveRequest = { projectId ->
                        requestViewModel.approveRequest(projectId, "", onSuccess = {})
                    },
                    onRejectRequest = { projectId ->
                        requestViewModel.rejectRequest(projectId, "Отклонено", onSuccess = {})
                    }
                )
            }
        }
    }
}

@Composable
private fun ConstructionObjectsScreenContainer() {
    var selectedObjectId by remember { mutableStateOf<String?>(null) }
    
    AnimatedContent(
        targetState = selectedObjectId,
        transitionSpec = {
            if (targetState != null) {
                slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { it }
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { -it }
                )
            } else {
                slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { -it }
                ) togetherWith slideOutHorizontally(
                    animationSpec = tween(300),
                    targetOffsetX = { it }
                )
            }
        }
    ) { objectId ->
        if (objectId != null) {
            ConstructionObjectDetailScreen(
                objectId = objectId,
                onBackClick = { selectedObjectId = null }
            )
        } else {
            ConstructionObjectsListScreen(
                onObjectClick = { id -> selectedObjectId = id }
            )
        }
    }
}

@Composable
private fun DocumentsScreenContainer() {
    var selectedDocumentId by remember { mutableStateOf<String?>(null) }
    
    Box {
        DocumentsListScreen(
            onDocumentClick = { documentId -> selectedDocumentId = documentId }
        )
        
        // Показываем диалог поверх списка с анимацией
        AnimatedVisibility(
            visible = selectedDocumentId != null,
            enter = fadeIn() + scaleIn(initialScale = 0.9f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f)
        ) {
            if (selectedDocumentId != null) {
                DocumentDetailDialog(
                    documentId = selectedDocumentId!!,
                    onDismiss = { selectedDocumentId = null }
                )
            }
        }
    }
}

private data class NavigationItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
