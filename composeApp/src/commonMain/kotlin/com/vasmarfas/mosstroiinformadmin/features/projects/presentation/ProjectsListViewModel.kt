package com.vasmarfas.mosstroiinformadmin.features.projects.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vasmarfas.mosstroiinformadmin.core.data.models.Project
import com.vasmarfas.mosstroiinformadmin.core.data.models.ProjectStatus
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult
import com.vasmarfas.mosstroiinformadmin.features.projects.data.ProjectsRepository
import com.vasmarfas.mosstroiinformadmin.features.completion.data.CompletionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

data class ProjectsListState(
    val isLoading: Boolean = false,
    val projects: List<Project> = emptyList(),
    val filteredProjects: List<Project> = emptyList(),
    val selectedFilter: ProjectStatus? = null,
    val error: String? = null,
    val completedProjects: Map<String, Boolean> = emptyMap() // Map projectId -> isCompleted
)

class ProjectsListViewModel(
    private val repository: ProjectsRepository,
    private val completionRepository: CompletionRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(ProjectsListState())
    val state: StateFlow<ProjectsListState> = _state.asStateFlow()
    
    // Кэш для проектов
    private var cachedProjects: List<Project>? = null
    // Флаг для отслеживания первого запроса
    private var isFirstLoad = true
    
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
            
            // Для первого запроса используем короткий таймаут (5 секунд), чтобы быстрее сделать retry
            val isFirstRequest = isFirstLoad && cachedProjects == null
            if (isFirstRequest) {
                isFirstLoad = false
            }
            
            val result = repository.getProjects(isFirstRequest = isFirstRequest)
            
            when (result) {
                is ApiResult.Success -> {
                    cachedProjects = result.data
                    // Загружаем completion status для проектов со статусом "construction"
                    val completedProjectsMap = loadCompletionStatuses(result.data)
                    _state.value = _state.value.copy(
                        isLoading = false,
                        projects = result.data,
                        filteredProjects = filterProjects(result.data, _state.value.selectedFilter),
                        completedProjects = completedProjectsMap
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
    
    private suspend fun loadCompletionStatuses(projects: List<Project>): Map<String, Boolean> = coroutineScope {
        // Загружаем completion status только для проектов со статусом "construction"
        val constructionProjects = projects.filter { it.status == "construction" }
        
        if (constructionProjects.isEmpty()) {
            return@coroutineScope emptyMap()
        }
        
        // Загружаем completion status параллельно для всех проектов
        val deferredResults = constructionProjects.map { project ->
            async {
                try {
                    when (val result = completionRepository.getCompletionStatus(project.id)) {
                        is ApiResult.Success -> project.id to result.data.isCompleted
                        else -> project.id to false
                    }
                } catch (e: Exception) {
                    project.id to false
                }
            }
        }
        
        deferredResults.awaitAll().toMap()
    }
}

