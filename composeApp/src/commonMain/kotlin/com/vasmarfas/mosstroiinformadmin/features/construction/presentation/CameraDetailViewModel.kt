package com.vasmarfas.mosstroiinformadmin.features.construction.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vasmarfas.mosstroiinformadmin.core.data.models.Camera
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult
import com.vasmarfas.mosstroiinformadmin.features.construction.data.ConstructionSitesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CameraDetailState(
    val isLoading: Boolean = false,
    val camera: Camera? = null,
    val error: String? = null
)

class CameraDetailViewModel(
    private val siteId: String,
    private val cameraId: String,
    private val repository: ConstructionSitesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CameraDetailState())
    val state: StateFlow<CameraDetailState> = _state.asStateFlow()
    
    private var currentSiteId: String? = null
    private var currentCameraId: String? = null

    init {
        currentSiteId = siteId
        currentCameraId = cameraId
        loadCamera()
    }
    
    // Проверяем, изменились ли siteId или cameraId, и перезагружаем камеру если нужно
    fun updateCameraId(newSiteId: String, newCameraId: String) {
        if (currentSiteId != newSiteId || currentCameraId != newCameraId) {
            // Сбрасываем состояние перед загрузкой новой камеры
            _state.value = CameraDetailState(isLoading = true)
            currentSiteId = newSiteId
            currentCameraId = newCameraId
            loadCamera()
        }
    }

    fun loadCamera() {
        viewModelScope.launch {
            val idToLoadSite = currentSiteId ?: siteId
            val idToLoadCamera = currentCameraId ?: cameraId
            _state.value = _state.value.copy(isLoading = true, error = null)

            when (val result = repository.getCamera(idToLoadSite, idToLoadCamera)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        camera = result.data
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

