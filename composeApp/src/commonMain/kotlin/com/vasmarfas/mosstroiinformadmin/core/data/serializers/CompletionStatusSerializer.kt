package com.vasmarfas.mosstroiinformadmin.core.data.serializers

import com.vasmarfas.mosstroiinformadmin.core.data.models.CompletionStatus
import com.vasmarfas.mosstroiinformadmin.core.data.models.FinalDocument
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*
import kotlinx.serialization.serializer

object CompletionStatusSerializer : KSerializer<CompletionStatus> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("CompletionStatus") {
        element<String>("projectId", isOptional = true)
        element<Boolean>("isCompleted", isOptional = true)
        element<Float>("progress", isOptional = true)
        element<Boolean>("allDocumentsSigned", isOptional = true)
        element<List<FinalDocument>>("documents", isOptional = true)
    }

    override fun serialize(encoder: Encoder, value: CompletionStatus) {
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.projectId)
            encodeBooleanElement(descriptor, 1, value.isCompleted)
            encodeFloatElement(descriptor, 2, value.progress)
            encodeBooleanElement(descriptor, 3, value.allDocumentsSigned)
            encodeSerializableElement(descriptor, 4, serializer<List<FinalDocument>>(), value.documents)
        }
    }

    override fun deserialize(decoder: Decoder): CompletionStatus {
        return decoder.decodeStructure(descriptor) {
            var projectId: String? = null
            var isCompleted: Boolean? = null
            var completionDate: Instant? = null
            var progress: Float? = null
            var allDocumentsSigned: Boolean? = null
            var documents: List<FinalDocument>? = null

            // Используем JsonDecoder для гибкого парсинга
            if (decoder is JsonDecoder) {
                val json = decoder.decodeJsonElement() as? JsonObject
                if (json != null) {
                    // Поддерживаем оба формата: camelCase и snake_case
                    projectId = json["projectId"]?.jsonPrimitive?.content
                        ?: json["project_id"]?.jsonPrimitive?.content
                        ?: ""
                    
                    isCompleted = json["isCompleted"]?.jsonPrimitive?.booleanOrNull
                        ?: json["is_completed"]?.jsonPrimitive?.booleanOrNull
                        ?: false
                    
                    completionDate = json["completionDate"]?.jsonPrimitive?.content?.let {
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
                    
                    progress = json["progress"]?.jsonPrimitive?.doubleOrNull?.toFloat()
                        ?: 0.0f
                    
                    allDocumentsSigned = json["allDocumentsSigned"]?.jsonPrimitive?.booleanOrNull
                        ?: json["all_documents_signed"]?.jsonPrimitive?.booleanOrNull
                        ?: false
                    
                    documents = json["documents"]?.jsonArray?.mapNotNull { element ->
                        try {
                            Json.decodeFromJsonElement(FinalDocument.serializer(), element)
                        } catch (e: Exception) {
                            null
                        }
                    } ?: emptyList()
                }
            } else {
                // Fallback для обычного декодера
                while (true) {
                    when (val index = decodeElementIndex(descriptor)) {
                        0 -> projectId = decodeStringElement(descriptor, 0)
                        1 -> isCompleted = decodeBooleanElement(descriptor, 1)
                        2 -> progress = decodeFloatElement(descriptor, 2)
                        3 -> allDocumentsSigned = decodeBooleanElement(descriptor, 3)
                        4 -> documents = decodeSerializableElement(descriptor, 4, serializer<List<FinalDocument>>())
                        CompositeDecoder.DECODE_DONE -> break
                        else -> error("Unexpected index: $index")
                    }
                }
            }

            CompletionStatus(
                projectId = projectId ?: "",
                isCompleted = isCompleted ?: false,
                completionDate = completionDate,
                progress = progress ?: 0.0f,
                allDocumentsSigned = allDocumentsSigned ?: false,
                documents = documents ?: emptyList()
            )
        }
    }
}

