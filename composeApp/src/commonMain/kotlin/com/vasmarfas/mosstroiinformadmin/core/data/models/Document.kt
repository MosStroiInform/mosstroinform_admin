package com.vasmarfas.mosstroiinformadmin.core.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Document(
    val id: String,
    @SerialName("project_id")
    val projectId: String,
    val title: String,
    val description: String? = null,
    @SerialName("file_url")
    val fileUrl: String? = null,
    val status: String,
    @SerialName("submitted_at")
    val submittedAt: String? = null,
    @SerialName("approved_at")
    val approvedAt: String? = null,
    @SerialName("signed_at")
    val signedAt: String? = null,
    @SerialName("signature_url")
    val signatureUrl: String? = null,
    @SerialName("rejection_reason")
    val rejectionReason: String? = null
)

enum class DocumentStatus(val value: String, val displayName: String) {
    PENDING("pending", "Ожидает"),
    UNDER_REVIEW("under_review", "На рассмотрении"),
    APPROVED("approved", "Одобрен"),
    REJECTED("rejected", "Отклонен");
    
    companion object {
        fun fromValue(value: String): DocumentStatus {
            return entries.find { it.value == value } ?: PENDING
        }
    }
}

@Serializable
data class RejectDocumentRequest(
    val reason: String
)

@Serializable
data class CreateDocumentRequest(
    val title: String,
    val description: String? = null,
    @SerialName("file_url")
    val fileUrl: String? = null
)

