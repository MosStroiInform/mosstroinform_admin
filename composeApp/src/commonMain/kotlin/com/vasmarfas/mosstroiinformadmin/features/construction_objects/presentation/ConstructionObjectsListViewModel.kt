package com.vasmarfas.mosstroiinformadmin.features.construction_objects.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vasmarfas.mosstroiinformadmin.core.data.models.ConstructionObject
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult
import com.vasmarfas.mosstroiinformadmin.features.construction_objects.data.ConstructionObjectsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConstructionObjectsListState(
    val isLoading: Boolean = false,
    val objects: List<ConstructionObject> = emptyList(),
    val error: String? = null
)

class ConstructionObjectsListViewModel(
    private val repository: ConstructionObjectsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ConstructionObjectsListState())
    val state: StateFlow<ConstructionObjectsListState> = _state.asStateFlow()

    init {
        loadObjects()
    }

    fun loadObjects() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = repository.getConstructionObjects()) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        objects = result.data
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
}

