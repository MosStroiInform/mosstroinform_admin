package com.vasmarfas.mosstroiinformadmin.features.construction.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vasmarfas.mosstroiinformadmin.core.data.models.ConstructionSite
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult
import com.vasmarfas.mosstroiinformadmin.features.construction.data.ConstructionSitesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SiteDetailState(
    val isLoading: Boolean = false,
    val site: ConstructionSite? = null,
    val error: String? = null
)

class SiteDetailViewModel(
    private val projectId: String,
    private val repository: ConstructionSitesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SiteDetailState())
    val state: StateFlow<SiteDetailState> = _state.asStateFlow()

    init {
        loadSite()
    }

    fun loadSite() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = repository.getSiteByProject(projectId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        site = result.data
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

