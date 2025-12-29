package com.vasmarfas.mosstroiinformadmin.features.construction.data

import com.vasmarfas.mosstroiinformadmin.core.data.models.Camera
import com.vasmarfas.mosstroiinformadmin.core.data.models.ConstructionSite
import com.vasmarfas.mosstroiinformadmin.core.network.ApiConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*

class ConstructionSitesApi(private val client: HttpClient) {

    suspend fun getSiteByProject(projectId: String): ConstructionSite {
        return client.get(ApiConfig.ConstructionSites.byProject(projectId)).body()
    }

    suspend fun getCameras(siteId: String): List<Camera> {
        return client.get(ApiConfig.ConstructionSites.cameras(siteId)).body()
    }

    suspend fun getCamera(siteId: String, cameraId: String): Camera {
        return client.get(ApiConfig.ConstructionSites.camera(siteId, cameraId)).body()
    }
}

