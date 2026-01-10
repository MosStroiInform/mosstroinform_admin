package com.vasmarfas.mosstroiinformadmin.features.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vasmarfas.mosstroiinformadmin.core.data.models.Project
import com.vasmarfas.mosstroiinformadmin.core.data.models.ProjectCreateRequest
import com.vasmarfas.mosstroiinformadmin.core.data.models.ProjectUpdateRequest
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult
import com.vasmarfas.mosstroiinformadmin.features.admin.data.AdminRepository
import com.vasmarfas.mosstroiinformadmin.features.projects.data.ProjectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProjectCreateEditState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val project: Project? = null
)

class ProjectCreateEditViewModel(
    private val repository: AdminRepository,
    private val projectsRepository: ProjectsRepository,
    private val projectId: String? = null
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectCreateEditState())
    val state: StateFlow<ProjectCreateEditState> = _state.asStateFlow()
    
    init {
        // Загружаем проект при редактировании
        if (projectId != null) {
            loadProject()
        }
    }
    
    private fun loadProject() {
        if (projectId == null) return
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            when (val result = projectsRepository.getProject(projectId)) {
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
                ApiResult.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }
            }
        }
    }

    fun createProject(
        name: String,
        address: String,
        description: String,
        area: Float,
        floors: Int,
        price: Float,
        bedrooms: Int,
        bathrooms: Int,
        imageUrl: String?,
        stages: List<String>
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null, success = false)

            val request = ProjectCreateRequest(
                name = name,
                address = address,
                description = description,
                area = area,
                floors = floors,
                price = price,
                bedrooms = bedrooms,
                bathrooms = bathrooms,
                imageUrl = imageUrl,
                stages = stages
            )

            when (val result = repository.createProject(request)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        success = true,
                        project = result.data
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        error = result.message
                    )
                }
                is ApiResult.Loading -> {
                    _state.value = _state.value.copy(isSaving = true)
                }
            }
        }
    }

    fun updateProject(
        name: String? = null,
        address: String? = null,
        description: String? = null,
        area: Float? = null,
        floors: Int? = null,
        price: Float? = null,
        bedrooms: Int? = null,
        bathrooms: Int? = null,
        imageUrl: String? = null,
        status: String? = null,
        stages: List<String>? = null
    ) {
        if (projectId == null) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null, success = false)

            val request = ProjectUpdateRequest(
                name = name,
                address = address,
                description = description,
                area = area,
                floors = floors,
                price = price,
                bedrooms = bedrooms,
                bathrooms = bathrooms,
                imageUrl = imageUrl,
                status = status,
                stages = stages
            )

            when (val result = repository.updateProject(projectId, request)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        success = true,
                        project = result.data
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        error = result.message
                    )
                }
                is ApiResult.Loading -> {
                    _state.value = _state.value.copy(isSaving = true)
                }
            }
        }
    }

    fun deleteProject(onSuccess: () -> Unit) {
        if (projectId == null) return

        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true, error = null)

            when (val result = repository.deleteProject(projectId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(isSaving = false, success = true)
                    onSuccess()
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isSaving = false,
                        error = result.message
                    )
                }
                is ApiResult.Loading -> {
                    _state.value = _state.value.copy(isSaving = true)
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun clearSuccess() {
        _state.value = _state.value.copy(success = false)
    }
}

