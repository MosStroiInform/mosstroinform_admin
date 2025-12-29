package com.vasmarfas.mosstroiinformadmin.features.completion.data

import com.vasmarfas.mosstroiinformadmin.core.data.models.CompletionStatus
import com.vasmarfas.mosstroiinformadmin.core.data.models.FinalDocument
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult

class CompletionRepository(private val api: CompletionApi) {

    suspend fun getCompletionStatus(projectId: String): ApiResult<CompletionStatus> {
        return try {
            val status = api.getCompletionStatus(projectId)
            ApiResult.Success(status)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка загрузки статуса завершения")
        }
    }

    suspend fun getFinalDocuments(projectId: String): ApiResult<List<FinalDocument>> {
        return try {
            val documents = api.getFinalDocuments(projectId)
            ApiResult.Success(documents)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка загрузки финальных документов")
        }
    }

    suspend fun signDocument(projectId: String, documentId: String): ApiResult<Unit> {
        return try {
            api.signDocument(projectId, documentId)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка подписания документа")
        }
    }

    suspend fun rejectDocument(projectId: String, documentId: String, reason: String): ApiResult<Unit> {
        return try {
            api.rejectDocument(projectId, documentId, reason)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка отклонения документа")
        }
    }
}

