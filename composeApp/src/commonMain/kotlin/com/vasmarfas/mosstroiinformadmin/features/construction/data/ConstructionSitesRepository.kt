package com.vasmarfas.mosstroiinformadmin.features.construction.data

import com.vasmarfas.mosstroiinformadmin.core.data.models.Camera
import com.vasmarfas.mosstroiinformadmin.core.data.models.ConstructionSite
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult

class ConstructionSitesRepository(private val api: ConstructionSitesApi) {

    suspend fun getSiteByProject(projectId: String): ApiResult<ConstructionSite> {
        return try {
            val site = api.getSiteByProject(projectId)
            ApiResult.Success(site)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка загрузки строительной площадки")
        }
    }

    suspend fun getCameras(siteId: String): ApiResult<List<Camera>> {
        return try {
            val cameras = api.getCameras(siteId)
            ApiResult.Success(cameras)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка загрузки камер")
        }
    }

    suspend fun getCamera(siteId: String, cameraId: String): ApiResult<Camera> {
        return try {
            val camera = api.getCamera(siteId, cameraId)
            ApiResult.Success(camera)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка загрузки камеры")
        }
    }
}

