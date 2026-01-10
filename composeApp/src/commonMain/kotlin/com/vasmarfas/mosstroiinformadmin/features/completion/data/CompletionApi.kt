package com.vasmarfas.mosstroiinformadmin.features.completion.data

import com.vasmarfas.mosstroiinformadmin.core.data.models.CompletionStatus
import com.vasmarfas.mosstroiinformadmin.core.data.models.FinalDocument
import com.vasmarfas.mosstroiinformadmin.core.data.models.FinalDocumentRejectRequest
import com.vasmarfas.mosstroiinformadmin.core.data.models.FinalDocumentCreateRequest
import com.vasmarfas.mosstroiinformadmin.core.data.models.ConstructionSite
import com.vasmarfas.mosstroiinformadmin.core.data.serializers.InstantSerializer
import com.vasmarfas.mosstroiinformadmin.core.network.ApiConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.datetime.Instant
import kotlinx.serialization.json.*

class CompletionApi(private val client: HttpClient) {

    suspend fun getCompletionStatus(projectId: String): CompletionStatus {
        // Получаем сырой JSON ответ
        val response = client.get(ApiConfig.Completion.status(projectId))
        val jsonString = try {
            response.body<String>()
        } catch (e: Exception) {
            // Если не получается как String, пробуем через JsonElement
            try {
                val jsonElement: JsonElement = response.body()
                jsonElement.toString()
            } catch (e2: Exception) {
                println("Ошибка получения ответа: ${e2.message}")
                "{}"
            }
        }
        
        // Логируем для отладки
        println("CompletionStatus JSON ответ: $jsonString")
        
        val json = try {
            Json.parseToJsonElement(jsonString) as? JsonObject
        } catch (e: Exception) {
            println("Ошибка парсинга JSON: ${e.message}")
            null
        }
        
        return if (json != null) {
            // Парсим вручную, поддерживая оба формата (camelCase и snake_case)
            val actualProjectId = json["projectId"]?.jsonPrimitive?.content
                ?: json["project_id"]?.jsonPrimitive?.content
                ?: projectId // Fallback на переданный projectId
            
            val actualIsCompleted = json["isCompleted"]?.jsonPrimitive?.booleanOrNull
                ?: json["is_completed"]?.jsonPrimitive?.booleanOrNull
                ?: false
            
            val completionDate = json["completionDate"]?.jsonPrimitive?.content?.let {
                try {
                    Instant.parse(it)
                } catch (e: Exception) {
                    null
                }
            } ?: json["completion_date"]?.jsonPrimitive?.content?.let {
                try {
                    Instant.parse(it)
                } catch (e: Exception) {
                    null
                }
            }
            
            val progress = json["progress"]?.jsonPrimitive?.doubleOrNull?.toFloat() ?: 0.0f
            
            val allDocumentsSigned = json["allDocumentsSigned"]?.jsonPrimitive?.booleanOrNull
                ?: json["all_documents_signed"]?.jsonPrimitive?.booleanOrNull
                ?: false
            
            val documents = try {
                val documentsArray = json["documents"]?.jsonArray
                println("Найдено документов: ${documentsArray?.size ?: 0}")
                
                documentsArray?.mapNotNull { docElement ->
                    try {
                        // Парсим документ вручную, поддерживая оба формата
                        val docJson = docElement as? JsonObject ?: return@mapNotNull null
                        
                        println("Парсинг документа: ${docJson.keys}")
                        
                        val docId = docJson["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
                        val docTitle = docJson["title"]?.jsonPrimitive?.content ?: ""
                        val docDescription = docJson["description"]?.jsonPrimitive?.content ?: ""
                        val docStatus = docJson["status"]?.jsonPrimitive?.content ?: "pending"
                        
                        val docFileUrl = docJson["fileUrl"]?.jsonPrimitive?.content
                            ?: docJson["file_url"]?.jsonPrimitive?.content
                        
                        // Парсим даты - поддерживаем ISO 8601 формат
                        val docSubmittedAt = docJson["submittedAt"]?.jsonPrimitive?.content?.let {
                            try {
                                Instant.parse(it)
                            } catch (e: Exception) {
                                println("Ошибка парсинга submittedAt: ${e.message}, значение: $it")
                                null
                            }
                        } ?: docJson["submitted_at"]?.jsonPrimitive?.content?.let {
                            try {
                                Instant.parse(it)
                            } catch (e: Exception) {
                                println("Ошибка парсинга submitted_at: ${e.message}, значение: $it")
                                null
                            }
                        }
                        
                        val docSignedAt = docJson["signedAt"]?.jsonPrimitive?.content?.let {
                            try {
                                Instant.parse(it)
                            } catch (e: Exception) {
                                println("Ошибка парсинга signedAt: ${e.message}, значение: $it")
                                null
                            }
                        } ?: docJson["signed_at"]?.jsonPrimitive?.content?.let {
                            try {
                                Instant.parse(it)
                            } catch (e: Exception) {
                                println("Ошибка парсинга signed_at: ${e.message}, значение: $it")
                                null
                            }
                        }
                        
                        val docSignatureUrl = docJson["signatureUrl"]?.jsonPrimitive?.content
                            ?: docJson["signature_url"]?.jsonPrimitive?.content
                        
                        val document = FinalDocument(
                            id = docId,
                            title = docTitle,
                            description = docDescription,
                            fileUrl = docFileUrl,
                            file_url = null,
                            status = docStatus,
                            submittedAt = docSubmittedAt,
                            submitted_at = null,
                            signedAt = docSignedAt,
                            signed_at = null,
                            signatureUrl = docSignatureUrl,
                            signature_url = null
                        )
                        
                        println("Успешно распарсен документ: ${document.id} - ${document.title}")
                        document
                    } catch (e: Exception) {
                        // Логируем ошибку парсинга документа, но продолжаем
                        println("Ошибка парсинга документа: ${e.message}")
                        println("Стек: ${e.stackTraceToString()}")
                        println("Элемент: ${docElement}")
                        null
                    }
                } ?: emptyList()
            } catch (e: Exception) {
                println("Ошибка парсинга массива документов: ${e.message}")
                println("Стек: ${e.stackTraceToString()}")
                emptyList()
            }
            
            println("Итого распарсено документов: ${documents.size}")
            
            CompletionStatus(
                projectId = actualProjectId,
                isCompleted = actualIsCompleted,
                completionDate = completionDate,
                progress = progress,
                allDocumentsSigned = allDocumentsSigned,
                documents = documents
            )
        } else {
            // Fallback на значения по умолчанию
            CompletionStatus(
                projectId = projectId,
                isCompleted = false,
                completionDate = null,
                progress = 0.0f,
                allDocumentsSigned = false,
                documents = emptyList()
            )
        }
    }

    suspend fun getFinalDocuments(projectId: String): List<FinalDocument> {
        return client.get(ApiConfig.Completion.finalDocuments(projectId)).body()
    }

    suspend fun createFinalDocument(projectId: String, request: FinalDocumentCreateRequest): FinalDocument {
        return client.post(ApiConfig.Completion.createFinalDocument(projectId)) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
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

    suspend fun completeProject(projectId: String) {
        // Получаем construction site по project ID
        val site: ConstructionSite = client.get(ApiConfig.ConstructionSites.byProject(projectId)).body()
        
        // Завершаем строительный объект
        client.post(ApiConfig.ConstructionObjects.complete(site.id))
    }
}

