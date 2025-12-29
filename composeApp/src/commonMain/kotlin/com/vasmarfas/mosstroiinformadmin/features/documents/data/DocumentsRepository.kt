package com.vasmarfas.mosstroiinformadmin.features.documents.data

import com.vasmarfas.mosstroiinformadmin.core.data.models.Document
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult

class DocumentsRepository(private val api: DocumentsApi) {
    
    suspend fun getDocuments(): ApiResult<List<Document>> {
        return try {
            val documents = api.getDocuments()
            ApiResult.Success(documents)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка загрузки документов")
        }
    }
    
    suspend fun getDocument(id: String): ApiResult<Document> {
        return try {
            val document = api.getDocument(id)
            ApiResult.Success(document)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка загрузки документа")
        }
    }
    
    suspend fun approveDocument(id: String): ApiResult<Unit> {
        return try {
            api.approveDocument(id)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка одобрения документа")
        }
    }
    
    suspend fun rejectDocument(id: String, reason: String): ApiResult<Unit> {
        return try {
            api.rejectDocument(id, reason)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка отклонения документа")
        }
    }
}

