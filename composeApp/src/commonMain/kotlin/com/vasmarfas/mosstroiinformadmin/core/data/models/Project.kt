package com.vasmarfas.mosstroiinformadmin.core.data.models

import com.vasmarfas.mosstroiinformadmin.core.data.serializers.ProjectSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = ProjectSerializer::class)
data class Project(
    val id: String,
    val name: String,
    val address: String,
    val description: String = "",
    val area: Double,
    val floors: Int,
    val price: Int,
    val bedrooms: Int = 0,
    val bathrooms: Int = 0,
    @SerialName("imageUrl")
    val imageUrl: String? = null,
    val status: String, // "available", "requested", "construction", "completed"
    val stages: List<ProjectStage> = emptyList(),
    @SerialName("objectId")
    val objectId: String? = null
)

@Serializable
data class ProjectStage(
    val id: String,
    val name: String,
    val status: String // "pending", "in_progress", "completed"
)

@Serializable
data class ProjectStartRequest(
    val address: String
)

// Helper enums for UI (not for serialization)
enum class ProjectStatus(val value: String, val displayName: String) {
    AVAILABLE("available", "Доступен"),
    REQUESTED("requested", "Запрошен"),
    IN_PROGRESS("construction", "В строительстве"),
    COMPLETED("completed", "Завершен");
    
    companion object {
        fun fromValue(value: String): ProjectStatus {
            return entries.find { it.value == value } ?: AVAILABLE
        }
    }
}

enum class StageStatus(val value: String, val displayName: String) {
    PENDING("pending", "Ожидание"),
    IN_PROGRESS("in_progress", "В процессе"),
    COMPLETED("completed", "Завершен");
    
    companion object {
        fun fromValue(value: String): StageStatus {
            return entries.find { it.value == value } ?: PENDING
        }
    }
}
