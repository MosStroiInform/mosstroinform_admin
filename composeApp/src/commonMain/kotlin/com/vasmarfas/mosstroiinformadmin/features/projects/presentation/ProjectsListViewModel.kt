package com.vasmarfas.mosstroiinformadmin.features.projects.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vasmarfas.mosstroiinformadmin.core.data.models.Project
import com.vasmarfas.mosstroiinformadmin.core.data.models.ProjectStatus
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult
import com.vasmarfas.mosstroiinformadmin.features.projects.data.ProjectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProjectsListState(
    val isLoading: Boolean = false,
    val projects: List<Project> = emptyList(),
    val filteredProjects: List<Project> = emptyList(),
    val selectedFilter: ProjectStatus? = null,
    val error: String? = null
)

class ProjectsListViewModel(
    private val repository: ProjectsRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(ProjectsListState())
    val state: StateFlow<ProjectsListState> = _state.asStateFlow()
    
    // Кэш для проектов
    private var cachedProjects: List<Project>? = null
    
    init {
        loadProjects()
    }
    
    fun loadProjects(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            // Показываем кэшированные данные сразу, если они есть и не требуется обновление
            if (!forceRefresh && cachedProjects != null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    projects = cachedProjects!!,
                    filteredProjects = filterProjects(cachedProjects!!, _state.value.selectedFilter)
                )
            } else {
                _state.value = _state.value.copy(isLoading = true, error = null)
            }
            
            val result = repository.getProjects()
            
            when (result) {
                is ApiResult.Success -> {
                    cachedProjects = result.data
                    _state.value = _state.value.copy(
                        isLoading = false,
                        projects = result.data,
                        filteredProjects = filterProjects(result.data, _state.value.selectedFilter)
                    )
                }
                is ApiResult.Error -> {
                    // При ошибке показываем кэш, если есть
                    if (cachedProjects != null) {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            projects = cachedProjects!!,
                            filteredProjects = filterProjects(cachedProjects!!, _state.value.selectedFilter)
                        )
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                is ApiResult.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }
    }
    
    fun setFilter(filter: ProjectStatus?) {
        _state.value = _state.value.copy(
            selectedFilter = filter,
            filteredProjects = filterProjects(_state.value.projects, filter)
        )
    }
    
    private fun filterProjects(projects: List<Project>, filter: ProjectStatus?): List<Project> {
        return if (filter == null) {
            projects
        } else {
            projects.filter { it.status == filter.value }
        }
    }
    
    fun refreshProjects() {
        loadProjects(forceRefresh = true)
    }
}

