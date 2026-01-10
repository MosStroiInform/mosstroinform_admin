package com.vasmarfas.mosstroiinformadmin.core.data.models

import com.vasmarfas.mosstroiinformadmin.core.data.serializers.StatisticsResponseSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ==================== ЗАПРОСЫ ====================

@Serializable
data class ProjectCreateRequest(
    val name: String,
    val address: String,
    val description: String = "",
    val area: Float,
    val floors: Int,
    val price: Float,
    val bedrooms: Int = 0,
    val bathrooms: Int = 0,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val stages: List<String> = emptyList()
)

@Serializable
data class ProjectUpdateRequest(
    val name: String? = null,
    val address: String? = null,
    val description: String? = null,
    val area: Float? = null,
    val floors: Int? = null,
    val price: Float? = null,
    val bedrooms: Int? = null,
    val bathrooms: Int? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val status: String? = null,
    val stages: List<String>? = null
)

@Serializable
data class StageCreateRequest(
    val name: String
)

@Serializable
data class StageUpdateRequest(
    val name: String? = null,
    val status: String? = null
)

@Serializable
data class RequestRejectRequest(
    val reason: String
)

@Serializable
data class ProgressUpdateRequest(
    val progress: Float
)

@Serializable
data class StageStatusUpdateRequest(
    val status: String
)

@Serializable
data class BatchApproveRequest(
    val ids: List<String>
)

@Serializable
data class BatchRejectRequest(
    val ids: List<String>,
    val reason: String
)

@Serializable
data class CameraCreateRequest(
    val name: String,
    val description: String? = null,
    @SerialName("stream_url")
    val streamUrl: String,
    @SerialName("thumbnail_url")
    val thumbnailUrl: String? = null
)

@Serializable
data class CameraUpdateRequest(
    val name: String? = null,
    val description: String? = null,
    @SerialName("stream_url")
    val streamUrl: String? = null,
    @SerialName("thumbnail_url")
    val thumbnailUrl: String? = null,
    @SerialName("is_active")
    val isActive: Boolean? = null
)

// ==================== ОТВЕТЫ ====================

@Serializable(with = StatisticsResponseSerializer::class)
data class StatisticsResponse(
    val totalProjects: Int = 0,
    val availableProjects: Int = 0,
    val requestedProjects: Int = 0,
    val inProgressProjects: Int = 0,
    val totalDocuments: Int = 0,
    val pendingDocuments: Int = 0,
    val approvedDocuments: Int = 0,
    val rejectedDocuments: Int = 0,
    val totalRevenue: Float = 0f,
    val averageProjectPrice: Float = 0f
)

@Serializable
data class NotificationResponse(
    val id: String,
    val type: String, // "new_request" | "new_document" | "new_message" | "camera_offline"
    val title: String,
    val message: String,
    @SerialName("projectId")
    val projectId: String? = null,
    @SerialName("documentId")
    val documentId: String? = null,
    @SerialName("chatId")
    val chatId: String? = null,
    @SerialName("isRead")
    val isRead: Boolean,
    @SerialName("createdAt")
    val createdAt: String
)

