package com.vasmarfas.mosstroiinformadmin.features.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vasmarfas.mosstroiinformadmin.core.data.models.Project
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult
import com.vasmarfas.mosstroiinformadmin.features.admin.data.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RequestManagementState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: String? = null
)

class RequestManagementViewModel(
    private val repository: AdminRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RequestManagementState())
    val state: StateFlow<RequestManagementState> = _state.asStateFlow()

    fun approveRequest(projectId: String, address: String, onSuccess: (Project) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, success = null)

            when (val result = repository.approveRequest(projectId, address)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        success = "Запрос одобрен"
                    )
                    onSuccess(result.data)
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

    fun rejectRequest(projectId: String, reason: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, success = null)

            when (val result = repository.rejectRequest(projectId, reason)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        success = "Запрос отклонен"
                    )
                    onSuccess()
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

    fun batchApproveRequests(ids: List<String>, onSuccess: (List<Project>) -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, success = null)

            when (val result = repository.batchApproveRequests(ids)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        success = "Одобрено проектов: ${result.data.size}"
                    )
                    onSuccess(result.data)
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

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, success = null)
    }
}

