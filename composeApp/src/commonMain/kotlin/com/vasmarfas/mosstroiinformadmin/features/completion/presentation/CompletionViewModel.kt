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
    val actionSuccess: Boolean = false
)

class CompletionViewModel(
    private val projectId: String,
    private val repository: CompletionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CompletionScreenState())
    val state: StateFlow<CompletionScreenState> = _state.asStateFlow()

    init {
        loadCompletionStatus()
    }

    fun loadCompletionStatus() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = repository.getCompletionStatus(projectId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        completionStatus = result.data
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
}

