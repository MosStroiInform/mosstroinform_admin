package com.vasmarfas.mosstroiinformadmin.features.documents.data

import com.vasmarfas.mosstroiinformadmin.core.data.models.Document
import com.vasmarfas.mosstroiinformadmin.core.data.models.RejectDocumentRequest
import com.vasmarfas.mosstroiinformadmin.core.network.ApiConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class DocumentsApi(private val client: HttpClient) {
    
    suspend fun getDocuments(): List<Document> {
        return client.get(ApiConfig.Documents.LIST).body()
    }
    
    suspend fun getDocument(id: String): Document {
        return client.get(ApiConfig.Documents.byId(id)).body()
    }
    
    suspend fun approveDocument(id: String) {
        client.post(ApiConfig.Documents.approve(id))
    }
    
    suspend fun rejectDocument(id: String, reason: String) {
        client.post(ApiConfig.Documents.reject(id)) {
            contentType(ContentType.Application.Json)
            setBody(RejectDocumentRequest(reason))
        }
    }
}

