package com.vasmarfas.mosstroiinformadmin.features.construction_objects.data

import com.vasmarfas.mosstroiinformadmin.core.data.models.ConstructionObject
import com.vasmarfas.mosstroiinformadmin.core.data.models.DocumentsStatusUpdateRequest
import com.vasmarfas.mosstroiinformadmin.core.network.ApiConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ConstructionObjectsApi(private val client: HttpClient) {

    suspend fun getConstructionObjects(): List<ConstructionObject> {
        return client.get(ApiConfig.ConstructionObjects.LIST).body()
    }

    suspend fun getConstructionObject(objectId: String): ConstructionObject {
        return client.get(ApiConfig.ConstructionObjects.byId(objectId)).body()
    }

    suspend fun completeConstructionObject(objectId: String) {
        client.post(ApiConfig.ConstructionObjects.complete(objectId))
    }

    suspend fun updateDocumentsStatus(projectId: String, allDocumentsSigned: Boolean) {
        client.patch(ApiConfig.ConstructionObjects.updateDocumentsStatus(projectId)) {
            contentType(ContentType.Application.Json)
            setBody(DocumentsStatusUpdateRequest(allDocumentsSigned))
        }
    }
}

