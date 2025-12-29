package com.vasmarfas.mosstroiinformadmin.features.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vasmarfas.mosstroiinformadmin.core.data.models.*
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult
import com.vasmarfas.mosstroiinformadmin.features.chats.data.ChatsRepository
import com.vasmarfas.mosstroiinformadmin.features.documents.data.DocumentsRepository
import com.vasmarfas.mosstroiinformadmin.features.projects.data.ProjectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardStats(
    val totalProjects: Int = 0,
    val availableProjects: Int = 0,
    val requestedProjects: Int = 0,
    val constructionProjects: Int = 0,
    val pendingDocuments: Int = 0,
    val unreadMessages: Int = 0
)

sealed class DashboardUiState {
    data object Loading : DashboardUiState()
    data class Success(
        val stats: DashboardStats,
        val recentProjects: List<Project>,
        val pendingDocuments: List<Document>,
        val recentChats: List<Chat>
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

class DashboardViewModel(
    private val projectsRepository: ProjectsRepository,
    private val documentsRepository: DocumentsRepository,
    private val chatsRepository: ChatsRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    // Кэш для данных
    private var cachedState: DashboardUiState.Success? = null
    
    init {
        loadDashboard()
    }
    
    fun loadDashboard(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            // Показываем кэшированные данные сразу, если они есть
            if (!forceRefresh && cachedState != null) {
                _uiState.value = cachedState!!
            } else {
                _uiState.value = DashboardUiState.Loading
            }
            
            val projectsResult = projectsRepository.getProjects()
            val documentsResult = documentsRepository.getDocuments()
            val chatsResult = chatsRepository.getChats()
            
            if (projectsResult is ApiResult.Error) {
                _uiState.value = DashboardUiState.Error(projectsResult.message)
                return@launch
            }
            
            val projects = (projectsResult as ApiResult.Success).data
            val documents = (documentsResult as? ApiResult.Success)?.data ?: emptyList()
            val chats = (chatsResult as? ApiResult.Success)?.data ?: emptyList()
            
            val stats = DashboardStats(
                totalProjects = projects.size,
                availableProjects = projects.count { it.status == "available" },
                requestedProjects = projects.count { it.status == "requested" },
                constructionProjects = projects.count { it.status == "construction" },
                pendingDocuments = documents.count { 
                    it.status == "pending" || it.status == "under_review" 
                },
                unreadMessages = chats.sumOf { it.unreadCount }
            )
            
            val successState = DashboardUiState.Success(
                stats = stats,
                recentProjects = projects.take(5),
                pendingDocuments = documents.filter { 
                    it.status == "pending" || it.status == "under_review" 
                }.take(5),
                recentChats = chats.sortedByDescending { it.lastMessageAt }.take(5)
            )
            cachedState = successState
            _uiState.value = successState
        }
    }
}

