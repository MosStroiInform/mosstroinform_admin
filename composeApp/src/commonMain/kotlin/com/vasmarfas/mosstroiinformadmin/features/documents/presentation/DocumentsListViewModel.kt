package com.vasmarfas.mosstroiinformadmin.features.documents.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vasmarfas.mosstroiinformadmin.core.data.models.Document
import com.vasmarfas.mosstroiinformadmin.core.data.models.DocumentStatus
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult
import com.vasmarfas.mosstroiinformadmin.features.documents.data.DocumentsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DocumentsListState(
    val isLoading: Boolean = false,
    val documents: List<Document> = emptyList(),
    val filteredDocuments: List<Document> = emptyList(),
    val error: String? = null,
    val selectedFilter: DocumentStatus? = null
)

class DocumentsListViewModel(
    private val repository: DocumentsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DocumentsListState())
    val state: StateFlow<DocumentsListState> = _state.asStateFlow()

    init {
        loadDocuments()
    }

    fun loadDocuments() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            when (val result = repository.getDocuments()) {
                is ApiResult.Success -> {
                    val documents = result.data
                    _state.value = _state.value.copy(
                        isLoading = false,
                        documents = documents,
                        filteredDocuments = applyFilter(documents, _state.value.selectedFilter)
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

    fun setFilter(status: DocumentStatus?) {
        _state.value = _state.value.copy(
            selectedFilter = status,
            filteredDocuments = applyFilter(_state.value.documents, status)
        )
    }

    private fun applyFilter(documents: List<Document>, filter: DocumentStatus?): List<Document> {
        return if (filter == null) {
            documents
        } else {
            documents.filter { it.status == filter.value }
        }
    }
}

