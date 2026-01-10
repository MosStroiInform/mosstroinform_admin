package com.vasmarfas.mosstroiinformadmin.features.construction_objects.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vasmarfas.mosstroiinformadmin.core.data.models.ConstructionObject
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult
import com.vasmarfas.mosstroiinformadmin.features.construction_objects.data.ConstructionObjectsRepository
import com.vasmarfas.mosstroiinformadmin.features.admin.data.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConstructionObjectDetailState(
    val isLoading: Boolean = false,
    val constructionObject: ConstructionObject? = null,
    val error: String? = null,
    val isCompleting: Boolean = false,
    val isUpdatingDocumentsStatus: Boolean = false,
    val updatingStageId: String? = null,
    val actionSuccess: Boolean = false
)

class ConstructionObjectDetailViewModel(
    private val objectId: String,
    private val repository: ConstructionObjectsRepository,
    private val adminRepository: AdminRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ConstructionObjectDetailState())
    val state: StateFlow<ConstructionObjectDetailState> = _state.asStateFlow()

    init {
        loadObject()
    }

    fun loadObject() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = repository.getConstructionObject(objectId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        constructionObject = result.data
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

    fun completeObject() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isCompleting = true)

            when (repository.completeConstructionObject(objectId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isCompleting = false,
                        actionSuccess = true
                    )
                    loadObject()
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isCompleting = false,
                        error = "Ошибка завершения объекта"
                    )
                }
                ApiResult.Loading -> {}
            }
        }
    }

    fun updateDocumentsStatus(allDocumentsSigned: Boolean) {
        viewModelScope.launch {
            val projectId = _state.value.constructionObject?.projectId ?: return@launch
            _state.value = _state.value.copy(isUpdatingDocumentsStatus = true)

            when (repository.updateDocumentsStatus(projectId, allDocumentsSigned)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isUpdatingDocumentsStatus = false,
                        actionSuccess = true
                    )
                    loadObject()
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isUpdatingDocumentsStatus = false,
                        error = "Ошибка обновления статуса документов"
                    )
                }
                ApiResult.Loading -> {}
            }
        }
    }

    fun clearActionSuccess() {
        _state.value = _state.value.copy(actionSuccess = false)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun updateStageStatus(stageId: String, status: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(updatingStageId = stageId, error = null, actionSuccess = false)

            when (adminRepository.updateStageStatus(objectId, stageId, status)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        updatingStageId = null,
                        actionSuccess = true
                    )
                    loadObject()
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

