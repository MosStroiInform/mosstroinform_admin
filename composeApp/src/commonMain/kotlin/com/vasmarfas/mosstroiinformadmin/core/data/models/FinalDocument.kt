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
    val status: String,
    @SerialName("submittedAt")
    @Serializable(with = InstantSerializer::class)
    val submittedAt: Instant? = null,
    @SerialName("signedAt")
    @Serializable(with = InstantSerializer::class)
    val signedAt: Instant? = null,
    @SerialName("signatureUrl")
    val signatureUrl: String? = null
)

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
    val projectId: String,
    @SerialName("isCompleted")
    val isCompleted: Boolean,
    @SerialName("completionDate")
    @Serializable(with = InstantSerializer::class)
    val completionDate: Instant? = null,
    val progress: Float,
    @SerialName("allDocumentsSigned")
    val allDocumentsSigned: Boolean = false,
    val documents: List<FinalDocument> = emptyList()
)

@Serializable
data class FinalDocumentRejectRequest(
    val reason: String
)
