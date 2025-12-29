package com.vasmarfas.mosstroiinformadmin.features.admin.data

import com.vasmarfas.mosstroiinformadmin.core.data.models.*
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult
import com.vasmarfas.mosstroiinformadmin.features.admin.data.AdminApi

class AdminRepository(private val adminApi: AdminApi) {
    
    // ==================== УПРАВЛЕНИЕ ПРОЕКТАМИ ====================
    
    suspend fun createProject(request: ProjectCreateRequest): ApiResult<Project> {
        return try {
            val project = adminApi.createProject(request)
            ApiResult.Success(project)
        } catch (e: Exception) {
            val errorMessage = when {
                e.message?.contains("Not Found") == true -> "Эндпоинт не найден. Убедитесь, что бэкенд запущен и перезапущен после изменений."
                e.message?.contains("detail") == true -> "Ошибка бэкенда: ${e.message}"
                else -> e.message ?: "Ошибка создания проекта"
            }
            ApiResult.Error(errorMessage)
        }
    }
    
    suspend fun updateProject(id: String, request: ProjectUpdateRequest): ApiResult<Project> {
        return try {
            val project = adminApi.updateProject(id, request)
            ApiResult.Success(project)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка обновления проекта")
        }
    }
    
    suspend fun deleteProject(id: String): ApiResult<Unit> {
        return try {
            adminApi.deleteProject(id)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка удаления проекта")
        }
    }
    
    // ==================== УПРАВЛЕНИЕ ЭТАПАМИ ====================
    
    suspend fun createStage(projectId: String, request: StageCreateRequest): ApiResult<ProjectStage> {
        return try {
            val stage = adminApi.createStage(projectId, request)
            ApiResult.Success(stage)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка создания этапа")
        }
    }
    
    suspend fun updateStage(projectId: String, stageId: String, request: StageUpdateRequest): ApiResult<ProjectStage> {
        return try {
            val stage = adminApi.updateStage(projectId, stageId, request)
            ApiResult.Success(stage)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка обновления этапа")
        }
    }
    
    suspend fun deleteStage(projectId: String, stageId: String): ApiResult<Unit> {
        return try {
            adminApi.deleteStage(projectId, stageId)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка удаления этапа")
        }
    }
    
    // ==================== УПРАВЛЕНИЕ ЗАПРОСАМИ ====================
    
    suspend fun approveRequest(projectId: String, address: String): ApiResult<Project> {
        return try {
            val project = adminApi.approveRequest(projectId, address)
            ApiResult.Success(project)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка одобрения запроса")
        }
    }
    
    suspend fun rejectRequest(projectId: String, reason: String): ApiResult<Unit> {
        return try {
            adminApi.rejectRequest(projectId, reason)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка отклонения запроса")
        }
    }
    
    suspend fun batchApproveRequests(ids: List<String>): ApiResult<List<Project>> {
        return try {
            val projects = adminApi.batchApproveRequests(ids)
            ApiResult.Success(projects)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка массового одобрения")
        }
    }
    
    // ==================== УПРАВЛЕНИЕ ПРОГРЕССОМ ====================
    
    suspend fun updateProgress(siteId: String, progress: Float): ApiResult<Unit> {
        return try {
            adminApi.updateProgress(siteId, progress)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка обновления прогресса")
        }
    }
    
    suspend fun updateStageStatus(objectId: String, stageId: String, status: String): ApiResult<Unit> {
        return try {
            adminApi.updateStageStatus(objectId, stageId, status)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка обновления статуса этапа")
        }
    }
    
    // ==================== АНАЛИТИКА ====================
    
    suspend fun getStatistics(): ApiResult<StatisticsResponse> {
        return try {
            val statistics = adminApi.getStatistics()
            ApiResult.Success(statistics)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка загрузки статистики")
        }
    }
    
    // ==================== МАССОВЫЕ ОПЕРАЦИИ ====================
    
    suspend fun batchApproveDocuments(ids: List<String>): ApiResult<Unit> {
        return try {
            adminApi.batchApproveDocuments(ids)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка массового одобрения документов")
        }
    }
    
    suspend fun batchRejectDocuments(ids: List<String>, reason: String): ApiResult<Unit> {
        return try {
            adminApi.batchRejectDocuments(ids, reason)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка массового отклонения документов")
        }
    }
    
    // ==================== УПРАВЛЕНИЕ КАМЕРАМИ ====================
    
    suspend fun createCamera(siteId: String, request: CameraCreateRequest): ApiResult<Camera> {
        return try {
            val camera = adminApi.createCamera(siteId, request)
            ApiResult.Success(camera)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка создания камеры")
        }
    }
    
    suspend fun updateCamera(siteId: String, cameraId: String, request: CameraUpdateRequest): ApiResult<Camera> {
        return try {
            val camera = adminApi.updateCamera(siteId, cameraId, request)
            ApiResult.Success(camera)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка обновления камеры")
        }
    }
    
    suspend fun deleteCamera(siteId: String, cameraId: String): ApiResult<Unit> {
        return try {
            adminApi.deleteCamera(siteId, cameraId)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка удаления камеры")
        }
    }
    
    // ==================== УВЕДОМЛЕНИЯ ====================
    
    suspend fun getNotifications(unreadOnly: Boolean = false): ApiResult<List<NotificationResponse>> {
        return try {
            val notifications = adminApi.getNotifications(unreadOnly)
            ApiResult.Success(notifications)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка загрузки уведомлений")
        }
    }
    
    suspend fun markNotificationRead(notificationId: String): ApiResult<Unit> {
        return try {
            adminApi.markNotificationRead(notificationId)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка отметки уведомления")
        }
    }
}

