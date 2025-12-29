package com.vasmarfas.mosstroiinformadmin.features.admin.data

import com.vasmarfas.mosstroiinformadmin.core.data.models.*
import com.vasmarfas.mosstroiinformadmin.core.network.ApiConfig
import com.vasmarfas.mosstroiinformadmin.core.data.models.Project
import com.vasmarfas.mosstroiinformadmin.core.data.models.ProjectStage
import com.vasmarfas.mosstroiinformadmin.core.data.models.ProjectStartRequest
import com.vasmarfas.mosstroiinformadmin.core.data.models.Camera
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AdminApi(private val client: HttpClient) {
    
    // ==================== УПРАВЛЕНИЕ ПРОЕКТАМИ ====================
    
    suspend fun createProject(request: ProjectCreateRequest): Project {
        val fullUrl = "${ApiConfig.BASE_URL}${ApiConfig.Admin.CREATE_PROJECT}"
        println("Creating project at URL: $fullUrl")
        println("Request body: ${request}")
        try {
            val response = client.post(ApiConfig.Admin.CREATE_PROJECT) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            println("Response status: ${response.status}")
            println("Response headers: ${response.headers}")
            if (response.status.value >= 400) {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    "Could not read error body: ${e.message}"
                }
                println("Error response body: $errorBody")
                throw Exception("HTTP ${response.status.value}: $errorBody")
            }
            return response.body()
        } catch (e: Exception) {
            println("Exception during createProject: ${e.message}")
            println("Stack trace: ${e.stackTraceToString()}")
            throw e
        }
    }
    
    suspend fun updateProject(id: String, request: ProjectUpdateRequest): Project {
        return client.put(ApiConfig.Admin.updateProject(id)) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun deleteProject(id: String) {
        client.delete(ApiConfig.Admin.deleteProject(id))
    }
    
    // ==================== УПРАВЛЕНИЕ ЭТАПАМИ ====================
    
    suspend fun createStage(projectId: String, request: StageCreateRequest): ProjectStage {
        return client.post(ApiConfig.Admin.createStage(projectId)) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun updateStage(projectId: String, stageId: String, request: StageUpdateRequest): ProjectStage {
        return client.put(ApiConfig.Admin.updateStage(projectId, stageId)) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun deleteStage(projectId: String, stageId: String) {
        client.delete(ApiConfig.Admin.deleteStage(projectId, stageId))
    }
    
    // ==================== УПРАВЛЕНИЕ ЗАПРОСАМИ ====================
    
    suspend fun approveRequest(projectId: String, address: String): Project {
        return client.post(ApiConfig.Admin.approveRequest(projectId)) {
            contentType(ContentType.Application.Json)
            setBody(ProjectStartRequest(address))
        }.body()
    }
    
    suspend fun rejectRequest(projectId: String, reason: String) {
        client.post(ApiConfig.Admin.rejectRequest(projectId)) {
            contentType(ContentType.Application.Json)
            setBody(RequestRejectRequest(reason))
        }
    }
    
    suspend fun batchApproveRequests(ids: List<String>): List<Project> {
        return client.post(ApiConfig.Admin.BATCH_APPROVE) {
            contentType(ContentType.Application.Json)
            setBody(BatchApproveRequest(ids))
        }.body()
    }
    
    // ==================== УПРАВЛЕНИЕ ПРОГРЕССОМ ====================
    
    suspend fun updateProgress(siteId: String, progress: Float) {
        client.patch(ApiConfig.Admin.updateProgress(siteId)) {
            contentType(ContentType.Application.Json)
            setBody(ProgressUpdateRequest(progress))
        }
    }
    
    suspend fun updateStageStatus(objectId: String, stageId: String, status: String) {
        client.patch(ApiConfig.Admin.updateStageStatus(objectId, stageId)) {
            contentType(ContentType.Application.Json)
            setBody(StageStatusUpdateRequest(status))
        }
    }
    
    // ==================== АНАЛИТИКА ====================
    
    suspend fun getStatistics(): StatisticsResponse {
        return client.get(ApiConfig.Admin.STATISTICS).body()
    }
    
    // ==================== МАССОВЫЕ ОПЕРАЦИИ ====================
    
    suspend fun batchApproveDocuments(ids: List<String>) {
        client.post(ApiConfig.Admin.BATCH_APPROVE_DOCUMENTS) {
            contentType(ContentType.Application.Json)
            setBody(BatchApproveRequest(ids))
        }
    }
    
    suspend fun batchRejectDocuments(ids: List<String>, reason: String) {
        client.post(ApiConfig.Admin.BATCH_REJECT_DOCUMENTS) {
            contentType(ContentType.Application.Json)
            setBody(BatchRejectRequest(ids, reason))
        }
    }
    
    // ==================== УПРАВЛЕНИЕ КАМЕРАМИ ====================
    
    suspend fun createCamera(siteId: String, request: CameraCreateRequest): Camera {
        return client.post(ApiConfig.Admin.createCamera(siteId)) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun updateCamera(siteId: String, cameraId: String, request: CameraUpdateRequest): Camera {
        return client.put(ApiConfig.Admin.updateCamera(siteId, cameraId)) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    suspend fun deleteCamera(siteId: String, cameraId: String) {
        client.delete(ApiConfig.Admin.deleteCamera(siteId, cameraId))
    }
    
    // ==================== УВЕДОМЛЕНИЯ ====================
    
    suspend fun getNotifications(unreadOnly: Boolean = false): List<NotificationResponse> {
        return client.get(ApiConfig.Admin.NOTIFICATIONS) {
            parameter("unread_only", unreadOnly)
        }.body()
    }
    
    suspend fun markNotificationRead(notificationId: String) {
        client.post(ApiConfig.Admin.markNotificationRead(notificationId))
    }
}

