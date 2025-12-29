package com.vasmarfas.mosstroiinformadmin.features.documents.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vasmarfas.mosstroiinformadmin.core.data.models.Document
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult
import com.vasmarfas.mosstroiinformadmin.features.documents.data.DocumentsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DocumentDetailState(
    val isLoading: Boolean = false,
    val document: Document? = null,
    val error: String? = null,
    val isApproving: Boolean = false,
    val isRejecting: Boolean = false,
    val actionSuccess: Boolean = false
)

class DocumentDetailViewModel(
    private val documentId: String,
    private val repository: DocumentsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DocumentDetailState())
    val state: StateFlow<DocumentDetailState> = _state.asStateFlow()

    init {
        loadDocument()
    }

    fun loadDocument() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            when (val result = repository.getDocument(documentId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        document = result.data
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

    fun approveDocument() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isApproving = true)
            
            when (repository.approveDocument(documentId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isApproving = false,
                        actionSuccess = true
                    )
                    loadDocument()
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isApproving = false,
                        error = "Ошибка одобрения документа"
                    )
                }
                ApiResult.Loading -> {}
            }
        }
    }

    fun rejectDocument(reason: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isRejecting = true)
            
            when (repository.rejectDocument(documentId, reason)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isRejecting = false,
                        actionSuccess = true
                    )
                    loadDocument()
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isRejecting = false,
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

