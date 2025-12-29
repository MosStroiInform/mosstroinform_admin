package com.vasmarfas.mosstroiinformadmin.features.projects.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vasmarfas.mosstroiinformadmin.core.data.models.Project
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult
import com.vasmarfas.mosstroiinformadmin.features.projects.data.ProjectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProjectDetailState(
    val isLoading: Boolean = false,
    val project: Project? = null,
    val error: String? = null,
    val isRequestingConstruction: Boolean = false,
    val isStartingConstruction: Boolean = false,
    val actionSuccess: Boolean = false
)

class ProjectDetailViewModel(
    private val projectId: String,
    private val repository: ProjectsRepository
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
            
            when (val result = repository.getProject(idToLoad)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        project = result.data
                    )
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
        loadProject()
    }
}

