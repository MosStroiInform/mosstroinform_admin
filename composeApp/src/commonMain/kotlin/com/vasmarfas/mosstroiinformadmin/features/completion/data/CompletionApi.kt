package com.vasmarfas.mosstroiinformadmin.features.completion.data

import com.vasmarfas.mosstroiinformadmin.core.data.models.CompletionStatus
import com.vasmarfas.mosstroiinformadmin.core.data.models.FinalDocument
import com.vasmarfas.mosstroiinformadmin.core.data.models.FinalDocumentRejectRequest
import com.vasmarfas.mosstroiinformadmin.core.network.ApiConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class CompletionApi(private val client: HttpClient) {

    suspend fun getCompletionStatus(projectId: String): CompletionStatus {
        return client.get(ApiConfig.Completion.status(projectId)).body()
    }

    suspend fun getFinalDocuments(projectId: String): List<FinalDocument> {
        return client.get(ApiConfig.Completion.finalDocuments(projectId)).body()
    }

    suspend fun signDocument(projectId: String, documentId: String) {
        client.post(ApiConfig.Completion.sign(projectId, documentId))
    }

    suspend fun rejectDocument(projectId: String, documentId: String, reason: String) {
        client.post(ApiConfig.Completion.rejectFinal(projectId, documentId)) {
            contentType(ContentType.Application.Json)
            setBody(FinalDocumentRejectRequest(reason))
        }
    }
}

