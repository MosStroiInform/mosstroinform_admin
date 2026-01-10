package com.vasmarfas.mosstroiinformadmin.core.data.models

import com.vasmarfas.mosstroiinformadmin.core.data.serializers.InstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.datetime.Instant

@Serializable
data class FinalDocument(
    val id: String,
    val title: String,
    val description: String = "",
    @SerialName("fileUrl")
    val fileUrl: String? = null,
    @SerialName("file_url")
    private val file_url: String? = null,
    val status: String,
    @SerialName("submittedAt")
    @Serializable(with = InstantSerializer::class)
    val submittedAt: Instant? = null,
    @SerialName("submitted_at")
    @Serializable(with = InstantSerializer::class)
    private val submitted_at: Instant? = null,
    @SerialName("signedAt")
    @Serializable(with = InstantSerializer::class)
    val signedAt: Instant? = null,
    @SerialName("signed_at")
    @Serializable(with = InstantSerializer::class)
    private val signed_at: Instant? = null,
    @SerialName("signatureUrl")
    val signatureUrl: String? = null,
    @SerialName("signature_url")
    private val signature_url: String? = null
) {
    // Вычисляемые свойства для совместимости с обоими форматами
    val actualFileUrl: String?
        get() = fileUrl ?: file_url
    
    val actualSubmittedAt: Instant?
        get() = submittedAt ?: submitted_at
    
    val actualSignedAt: Instant?
        get() = signedAt ?: signed_at
    
    val actualSignatureUrl: String?
        get() = signatureUrl ?: signature_url
}

enum class FinalDocumentStatus(val value: String, val displayName: String) {
    PENDING("pending", "Ожидает подписи"),
    SIGNED("signed", "Подписан"),
    REJECTED("rejected", "Отклонен");
    
    companion object {
        fun fromValue(value: String): FinalDocumentStatus {
            return entries.find { it.value == value } ?: PENDING
        }
    }
}

@Serializable
data class CompletionStatus(
    @SerialName("projectId")
    val projectId: String = "",
    @SerialName("project_id")
    private val project_id: String? = null,
    @SerialName("isCompleted")
    val isCompleted: Boolean = false,
    @SerialName("is_completed")
    private val is_completed: Boolean? = null,
    @SerialName("completionDate")
    @Serializable(with = InstantSerializer::class)
    val completionDate: Instant? = null,
    @SerialName("completion_date")
    @Serializable(with = InstantSerializer::class)
    private val completion_date: Instant? = null,
    val progress: Float = 0.0f,
    @SerialName("allDocumentsSigned")
    val allDocumentsSigned: Boolean = false,
    @SerialName("all_documents_signed")
    private val all_documents_signed: Boolean? = null,
    val documents: List<FinalDocument> = emptyList()
) {
    // Вычисляемые свойства для совместимости с обоими форматами
    val actualProjectId: String
        get() = projectId.ifEmpty { project_id ?: "" }
    
    val actualIsCompleted: Boolean
        get() = isCompleted || (is_completed ?: false)
    
    val actualCompletionDate: Instant?
        get() = completionDate ?: completion_date
    
    val actualAllDocumentsSigned: Boolean
        get() = allDocumentsSigned || (all_documents_signed ?: false)
}

@Serializable
data class FinalDocumentRejectRequest(
    val reason: String
)

@Serializable
data class FinalDocumentCreateRequest(
    val title: String,
    val description: String = "",
    @SerialName("fileUrl")
    val fileUrl: String? = null
)
