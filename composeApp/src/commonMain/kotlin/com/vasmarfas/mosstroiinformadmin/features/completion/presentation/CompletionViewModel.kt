package com.vasmarfas.mosstroiinformadmin.features.completion.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vasmarfas.mosstroiinformadmin.core.data.models.CompletionStatus
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult
import com.vasmarfas.mosstroiinformadmin.features.completion.data.CompletionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CompletionScreenState(
    val isLoading: Boolean = false,
    val completionStatus: CompletionStatus? = null,
    val error: String? = null,
    val actionInProgress: String? = null,
    val isCompleting: Boolean = false,
    val isCreatingDocument: Boolean = false,
    val actionSuccess: Boolean = false
)

class CompletionViewModel(
    private val projectId: String,
    private val repository: CompletionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CompletionScreenState())
    val state: StateFlow<CompletionScreenState> = _state.asStateFlow()
    
    private var currentProjectId: String? = null

    init {
        currentProjectId = projectId
        loadCompletionStatus()
    }
    
    fun updateProjectId(newProjectId: String) {
        if (newProjectId != currentProjectId) {
            currentProjectId = newProjectId
            // Сбрасываем состояние при смене проекта
            _state.value = CompletionScreenState()
            loadCompletionStatus()
        }
    }

    fun loadCompletionStatus() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = repository.getCompletionStatus(projectId)) {
                is ApiResult.Success -> {
                    // Если документы не загрузились из completion-status, попробуем загрузить отдельно
                    val status = result.data
                    val documentsResult = repository.getFinalDocuments(projectId)
                    
                    val finalStatus = if (documentsResult is ApiResult.Success && documentsResult.data.isNotEmpty() && status.documents.isEmpty()) {
                        // Если документы есть в отдельном эндпоинте, но не в completion-status, используем их
                        status.copy(documents = documentsResult.data)
                    } else if (status.documents.isEmpty() && documentsResult is ApiResult.Success) {
                        // Если документы не загрузились из completion-status, используем из отдельного эндпоинта
                        status.copy(documents = documentsResult.data)
                    } else {
                        status
                    }
                    
                    _state.value = _state.value.copy(
                        isLoading = false,
                        completionStatus = finalStatus
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

    fun signDocument(documentId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionInProgress = documentId)

            when (repository.signDocument(projectId, documentId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        actionInProgress = null,
                        actionSuccess = true
                    )
                    loadCompletionStatus()
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        actionInProgress = null,
                        error = "Ошибка подписания документа"
                    )
                }
                ApiResult.Loading -> {}
            }
        }
    }

    fun rejectDocument(documentId: String, reason: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionInProgress = documentId)

            when (repository.rejectDocument(projectId, documentId, reason)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        actionInProgress = null,
                        actionSuccess = true
                    )
                    loadCompletionStatus()
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        actionInProgress = null,
                        error = "Ошибка отклонения документа"
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

    fun createFinalDocument(title: String, description: String, fileUrl: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isCreatingDocument = true, error = null, actionSuccess = false)

            when (val result = repository.createFinalDocument(projectId, title, description, fileUrl)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isCreatingDocument = false,
                        actionSuccess = true
                    )
                    loadCompletionStatus()
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isCreatingDocument = false,
                        error = "Ошибка создания документа: ${result.message}"
                    )
                }
                ApiResult.Loading -> {}
            }
        }
    }

    fun completeProject() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isCompleting = true, error = null, actionSuccess = false)

            when (val result = repository.completeProject(projectId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isCompleting = false,
                        actionSuccess = true
                    )
                    loadCompletionStatus()
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isCompleting = false,
                        error = "Ошибка завершения проекта: ${result.message}"
                    )
                }
                ApiResult.Loading -> {}
            }
        }
    }
}

