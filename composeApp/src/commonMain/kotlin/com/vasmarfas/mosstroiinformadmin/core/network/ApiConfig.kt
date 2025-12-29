package com.vasmarfas.mosstroiinformadmin.core.network

object ApiConfig {
    const val BASE_URL = "https://mosstroiinform.vasmarfas.com"
    const val API_VERSION = "/api/v1"
    const val WS_URL = "wss://mosstroiinform.vasmarfas.com"
    const val WS_PORT = "8080"
    
    // Timeouts
    const val REQUEST_TIMEOUT = 30_000L
    const val CONNECT_TIMEOUT = 30_000L
    const val SOCKET_TIMEOUT = 30_000L
    
    // Endpoints
    object Auth {
        const val LOGIN = "$API_VERSION/auth/login"
        const val REGISTER = "$API_VERSION/auth/register"
        const val ME = "$API_VERSION/auth/me"
        const val REFRESH = "$API_VERSION/auth/refresh"
    }
    
    object Projects {
        const val LIST = "$API_VERSION/projects"
        const val REQUESTED = "$API_VERSION/projects/requested"
        fun byId(id: String) = "$API_VERSION/projects/$id"
        fun request(id: String) = "$API_VERSION/projects/$id/request"
        fun start(id: String) = "$API_VERSION/projects/$id/start"
    }
    
    object Documents {
        const val LIST = "$API_VERSION/documents"
        fun byId(id: String) = "$API_VERSION/documents/$id"
        fun approve(id: String) = "$API_VERSION/documents/$id/approve"
        fun reject(id: String) = "$API_VERSION/documents/$id/reject"
    }
    
    object ConstructionSites {
        fun byProject(projectId: String) = "$API_VERSION/construction-sites/project/$projectId"
        fun cameras(siteId: String) = "$API_VERSION/construction-sites/$siteId/cameras"
        fun camera(siteId: String, cameraId: String) = "$API_VERSION/construction-sites/$siteId/cameras/$cameraId"
    }
    
    object ConstructionObjects {
        const val LIST = "$API_VERSION/construction-objects"
        fun byId(objectId: String) = "$API_VERSION/construction-objects/$objectId"
        fun complete(objectId: String) = "$API_VERSION/construction-objects/$objectId/complete"
        fun updateDocumentsStatus(projectId: String) = "$API_VERSION/construction-objects/by-project/$projectId/documents-status"
    }
    
    object Chats {
        const val LIST = "$API_VERSION/chats"
        fun byId(chatId: String) = "$API_VERSION/chats/$chatId"
        fun messages(chatId: String) = "$API_VERSION/chats/$chatId/messages"
        fun markAsRead(chatId: String) = "$API_VERSION/chats/$chatId/messages/read"
        fun webSocket(chatId: String) = "/chat/$chatId"
    }
    
    object Completion {
        fun status(projectId: String) = "$API_VERSION/projects/$projectId/completion-status"
        fun finalDocuments(projectId: String) = "$API_VERSION/projects/$projectId/final-documents"
        fun finalDocument(projectId: String, documentId: String) = "$API_VERSION/projects/$projectId/final-documents/$documentId"
        fun sign(projectId: String, documentId: String) = "$API_VERSION/projects/$projectId/final-documents/$documentId/sign"
        fun rejectFinal(projectId: String, documentId: String) = "$API_VERSION/projects/$projectId/final-documents/$documentId/reject"
    }
    
    object Admin {
        // Управление проектами
        const val PROJECTS = "$API_VERSION/admin/projects"
        const val CREATE_PROJECT = "$API_VERSION/admin/projects"
        fun updateProject(id: String) = "$API_VERSION/admin/projects/$id"
        fun deleteProject(id: String) = "$API_VERSION/admin/projects/$id"
        
        // Управление этапами
        fun createStage(projectId: String) = "$API_VERSION/admin/$projectId/stages"
        fun updateStage(projectId: String, stageId: String) = "$API_VERSION/admin/$projectId/stages/$stageId"
        fun deleteStage(projectId: String, stageId: String) = "$API_VERSION/admin/$projectId/stages/$stageId"
        
        // Управление запросами
        fun approveRequest(projectId: String) = "$API_VERSION/admin/$projectId/approve-request"
        fun rejectRequest(projectId: String) = "$API_VERSION/admin/$projectId/reject-request"
        const val BATCH_APPROVE = "$API_VERSION/admin/batch-approve"
        
        // Управление прогрессом
        fun updateProgress(siteId: String) = "$API_VERSION/admin/construction-sites/$siteId/progress"
        fun updateStageStatus(objectId: String, stageId: String) = "$API_VERSION/admin/construction-objects/$objectId/stages/$stageId/status"
        
        // Аналитика
        const val STATISTICS = "$API_VERSION/admin/statistics"
        
        // Массовые операции
        const val BATCH_APPROVE_DOCUMENTS = "$API_VERSION/admin/documents/batch-approve"
        const val BATCH_REJECT_DOCUMENTS = "$API_VERSION/admin/documents/batch-reject"
        
        // Управление камерами
        fun createCamera(siteId: String) = "$API_VERSION/admin/construction-sites/$siteId/cameras"
        fun updateCamera(siteId: String, cameraId: String) = "$API_VERSION/admin/construction-sites/$siteId/cameras/$cameraId"
        fun deleteCamera(siteId: String, cameraId: String) = "$API_VERSION/admin/construction-sites/$siteId/cameras/$cameraId"
        
        // Уведомления
        const val NOTIFICATIONS = "$API_VERSION/admin/notifications"
        fun markNotificationRead(notificationId: String) = "$API_VERSION/admin/notifications/$notificationId/read"
    }
}

