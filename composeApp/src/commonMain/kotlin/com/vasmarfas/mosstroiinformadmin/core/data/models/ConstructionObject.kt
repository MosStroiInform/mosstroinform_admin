package com.vasmarfas.mosstroiinformadmin.core.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConstructionObjectStage(
    val id: String,
    val name: String,
    val status: String
)

@Serializable
data class ConstructionObject(
    val id: String,
    @SerialName("project_id")
    val projectId: String,
    val name: String,
    val address: String,
    val description: String = "",
    val area: Float,
    val floors: Int,
    val bedrooms: Int = 0,
    val bathrooms: Int = 0,
    val price: Int,
    @SerialName("imageUrl")
    val imageUrl: String? = null,
    val stages: List<ConstructionObjectStage> = emptyList(),
    @SerialName("chatId")
    val chatId: String? = null,
    @SerialName("allDocumentsSigned")
    val allDocumentsSigned: Boolean = false,
    @SerialName("isCompleted")
    val isCompleted: Boolean = false
)

@Serializable
data class DocumentsStatusUpdateRequest(
    @SerialName("allDocumentsSigned")
    val allDocumentsSigned: Boolean
)

