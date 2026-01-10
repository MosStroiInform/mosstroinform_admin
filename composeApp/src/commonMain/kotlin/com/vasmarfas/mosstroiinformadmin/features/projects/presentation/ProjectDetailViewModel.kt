package com.vasmarfas.mosstroiinformadmin.features.projects.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vasmarfas.mosstroiinformadmin.core.data.models.Project
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult
import com.vasmarfas.mosstroiinformadmin.features.projects.data.ProjectsRepository
import com.vasmarfas.mosstroiinformadmin.features.construction.data.ConstructionSitesRepository
import com.vasmarfas.mosstroiinformadmin.features.admin.data.AdminRepository
import com.vasmarfas.mosstroiinformadmin.features.completion.data.CompletionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProjectDetailState(
    val isLoading: Boolean = false,
    val project: Project? = null,
    val isCompleted: Boolean = false, // Флаг завершения проекта из completion status
    val error: String? = null,
    val isRequestingConstruction: Boolean = false,
    val isStartingConstruction: Boolean = false,
    val updatingStageId: String? = null,
    val actionSuccess: Boolean = false
)

class ProjectDetailViewModel(
    private val projectId: String,
    private val repository: ProjectsRepository,
    private val sitesRepository: ConstructionSitesRepository,
    private val adminRepository: AdminRepository,
    private val completionRepository: CompletionRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(ProjectDetailState())
    val state: StateFlow<ProjectDetailState> = _state.asStateFlow()
    
    private var currentProjectId: String? = null
    
    init {
        currentProjectId = projectId
        loadProject()
    }
    
    // Проверяем, изменился ли projectId, и перезагружаем проект если нужно
    fun updateProjectId(newProjectId: String) {
        if (currentProjectId != newProjectId) {
            // Сбрасываем состояние перед загрузкой нового проекта
            _state.value = ProjectDetailState(isLoading = true)
            currentProjectId = newProjectId
            loadProject()
        }
    }
    
    private fun loadProject() {
        viewModelScope.launch {
            val idToLoad = currentProjectId ?: projectId
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            // Загружаем проект
            when (val result = repository.getProject(idToLoad)) {
                is ApiResult.Success -> {
                    val project = result.data
                    _state.value = _state.value.copy(
                        isLoading = false,
                        project = project
                    )
                    
                    // Загружаем completion status для проверки is_completed
                    loadCompletionStatus(idToLoad)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is ApiResult.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }
    }
    
    private fun loadCompletionStatus(projectId: String) {
        viewModelScope.launch {
            // Загружаем completion status для проверки is_completed
            when (val result = completionRepository.getCompletionStatus(projectId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isCompleted = result.data.isCompleted
                    )
                }
                is ApiResult.Error -> {
                    // Если не удалось загрузить completion status, считаем что проект не завершен
                    _state.value = _state.value.copy(
                        isCompleted = false
                    )
                }
                ApiResult.Loading -> {}
            }
        }
    }
    
    fun requestConstruction() {
        viewModelScope.launch {
            val idToUse = currentProjectId ?: projectId
            _state.value = _state.value.copy(isRequestingConstruction = true, error = null, actionSuccess = false)
            
            when (val result = repository.requestConstruction(idToUse)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isRequestingConstruction = false,
                        actionSuccess = true
                    )
                    // Перезагружаем проект чтобы обновить статус
                    loadProject()
                    loadCompletionStatus(idToUse)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isRequestingConstruction = false,
                        error = result.message
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }
    
    fun startConstruction(address: String) {
        viewModelScope.launch {
            val idToUse = currentProjectId ?: projectId
            _state.value = _state.value.copy(isStartingConstruction = true, error = null, actionSuccess = false)
            
            when (val result = repository.startConstruction(idToUse, address)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isStartingConstruction = false,
                        actionSuccess = true
                    )
                    // Перезагружаем проект
                    loadProject()
                    loadCompletionStatus(idToUse)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isStartingConstruction = false,
                        error = result.message
                    )
                }
                is ApiResult.Loading -> {}
            }
        }
    }
    
    fun clearActionSuccess() {
        _state.value = _state.value.copy(actionSuccess = false)
    }
    
    fun refreshProject() {
        val idToLoad = currentProjectId ?: projectId
        loadProject()
        loadCompletionStatus(idToLoad)
    }

    fun updateStageStatus(stageId: String, status: String) {
        viewModelScope.launch {
            val idToUse = currentProjectId ?: projectId
            _state.value = _state.value.copy(updatingStageId = stageId, error = null, actionSuccess = false)

            // Получаем construction site ID по project ID
            val siteResult = sitesRepository.getSiteByProject(idToUse)
            if (siteResult !is ApiResult.Success) {
                _state.value = _state.value.copy(
                    updatingStageId = null,
                    error = "Не удалось найти строительную площадку для проекта"
                )
                return@launch
            }

            val siteId = siteResult.data.id

            when (adminRepository.updateStageStatus(siteId, stageId, status)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        updatingStageId = null,
                        actionSuccess = true
                    )
                    loadProject()
                    loadCompletionStatus(idToUse) // Перезагружаем completion status, так как прогресс мог измениться
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        updatingStageId = null,
                        error = "Ошибка обновления статуса этапа"
                    )
                }
                ApiResult.Loading -> {}
            }
        }
    }
}

