package com.vasmarfas.mosstroiinformadmin.features.construction_objects.data

import com.vasmarfas.mosstroiinformadmin.core.data.models.ConstructionObject
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult

class ConstructionObjectsRepository(private val api: ConstructionObjectsApi) {

    suspend fun getConstructionObjects(): ApiResult<List<ConstructionObject>> {
        return try {
            val objects = api.getConstructionObjects()
            ApiResult.Success(objects)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка загрузки объектов строительства")
        }
    }

    suspend fun getConstructionObject(objectId: String): ApiResult<ConstructionObject> {
        return try {
            val obj = api.getConstructionObject(objectId)
            ApiResult.Success(obj)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка загрузки объекта")
        }
    }

    suspend fun completeConstructionObject(objectId: String): ApiResult<Unit> {
        return try {
            api.completeConstructionObject(objectId)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка завершения объекта")
        }
    }

    suspend fun updateDocumentsStatus(projectId: String, allDocumentsSigned: Boolean): ApiResult<Unit> {
        return try {
            api.updateDocumentsStatus(projectId, allDocumentsSigned)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка обновления статуса документов")
        }
    }
}

