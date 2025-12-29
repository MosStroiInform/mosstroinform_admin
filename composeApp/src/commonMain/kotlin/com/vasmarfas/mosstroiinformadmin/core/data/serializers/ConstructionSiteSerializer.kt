package com.vasmarfas.mosstroiinformadmin.core.data.serializers

import com.vasmarfas.mosstroiinformadmin.core.data.models.ConstructionSite
import com.vasmarfas.mosstroiinformadmin.core.data.models.Camera
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

object ConstructionSiteSerializer : KSerializer<ConstructionSite> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ConstructionSite") {
        element<String>("id")
        element<String>("projectId")
        element<String>("projectName")
        element<String>("address")
        element<List<Camera>>("cameras")
        element<String?>("startDate", isOptional = true)
        element<String?>("expectedCompletionDate", isOptional = true)
        element<Float>("progress")
    }

    override fun serialize(encoder: Encoder, value: ConstructionSite) {
        val jsonEncoder = encoder as? JsonEncoder ?: return
        jsonEncoder.encodeJsonElement(buildJsonObject {
            put("id", value.id)
            put("projectId", value.projectId)
            put("projectName", value.projectName)
            put("address", value.address)
            putJsonArray("cameras") {
                value.cameras.forEach { camera ->
                    add(buildJsonObject {
                        put("id", camera.id)
                        put("name", camera.name)
                        put("description", camera.description)
                        put("streamUrl", camera.streamUrl)
                        put("isActive", camera.isActive)
                        camera.thumbnailUrl?.let { put("thumbnailUrl", it) }
                    })
                }
            }
            value.startDate?.toString()?.let { put("startDate", it) }
            value.expectedCompletionDate?.toString()?.let { put("expectedCompletionDate", it) }
            put("progress", value.progress)
        })
    }

    override fun deserialize(decoder: Decoder): ConstructionSite {
        val jsonDecoder = decoder as? JsonDecoder ?: throw IllegalArgumentException("Expected JsonDecoder")
        val json = jsonDecoder.decodeJsonElement().jsonObject

        fun getString(key: String): String {
            // Пробуем camelCase
            json[key]?.jsonPrimitive?.contentOrNull?.let { return it }
            // Пробуем snake_case
            val snakeCase = key.replace(Regex("([A-Z])")) { "_${it.groupValues[1].lowercase()}" }
            json[snakeCase]?.jsonPrimitive?.contentOrNull?.let { return it }
            // Пробуем lowercase
            json[key.lowercase()]?.jsonPrimitive?.contentOrNull?.let { return it }
            // Пробуем все ключи в json (регистронезависимый поиск)
            for (jsonKey in json.keys) {
                val jsonKeyLower = jsonKey.lowercase()
                if (jsonKeyLower == key.lowercase() || jsonKeyLower == snakeCase.lowercase()) {
                    val value = json[jsonKey]?.jsonPrimitive?.contentOrNull
                    if (value != null) {
                        return value
                    }
                }
            }
            // Для projectId и projectName делаем опциональными, если не найдены
            if (key == "projectId" || key == "projectName") {
                return ""
            }
            throw IllegalArgumentException("Missing required field: $key")
        }

        fun getStringOrNull(key: String): String? {
            json[key]?.jsonPrimitive?.contentOrNull?.let { return it }
            val snakeCase = key.replace(Regex("([A-Z])")) { "_${it.groupValues[1].lowercase()}" }
            json[snakeCase]?.jsonPrimitive?.contentOrNull?.let { return it }
            json[key.lowercase()]?.jsonPrimitive?.contentOrNull?.let { return it }
            return null
        }

        fun getFloat(key: String): Float {
            json[key]?.jsonPrimitive?.floatOrNull?.let { return it }
            val snakeCase = key.replace(Regex("([A-Z])")) { "_${it.groupValues[1].lowercase()}" }
            json[snakeCase]?.jsonPrimitive?.floatOrNull?.let { return it }
            json[key.lowercase()]?.jsonPrimitive?.floatOrNull?.let { return it }
            return 0.0f
        }

        fun getCameras(): List<Camera> {
            val camerasJson = json["cameras"]?.jsonArray
            if (camerasJson == null) return emptyList()

            return camerasJson.mapNotNull { cameraJson ->
                val cameraObj = cameraJson.jsonObject
                try {
                    Camera(
                        id = cameraObj["id"]?.jsonPrimitive?.content ?: "",
                        name = cameraObj["name"]?.jsonPrimitive?.content ?: "",
                        description = cameraObj["description"]?.jsonPrimitive?.contentOrNull ?: "",
                        streamUrl = cameraObj["streamUrl"]?.jsonPrimitive?.content
                            ?: cameraObj["stream_url"]?.jsonPrimitive?.content
                            ?: "",
                        isActive = cameraObj["isActive"]?.jsonPrimitive?.booleanOrNull
                            ?: cameraObj["is_active"]?.jsonPrimitive?.booleanOrNull
                            ?: true,
                        thumbnailUrl = cameraObj["thumbnailUrl"]?.jsonPrimitive?.contentOrNull
                            ?: cameraObj["thumbnail_url"]?.jsonPrimitive?.contentOrNull
                    )
                } catch (e: Exception) {
                    println("Error deserializing camera: ${e.message}")
                    null
                }
            }
        }

        // Парсим даты через InstantSerializer
        val startDate = getStringOrNull("startDate")?.let {
            try {
                kotlinx.datetime.Instant.parse(it)
            } catch (e: Exception) {
                null
            }
        }

        val expectedCompletionDate = getStringOrNull("expectedCompletionDate")?.let {
            try {
                kotlinx.datetime.Instant.parse(it)
            } catch (e: Exception) {
                null
            }
        }

        return ConstructionSite(
            id = getString("id"),
            projectId = getString("projectId"),
            projectName = getString("projectName"),
            address = getString("address"),
            cameras = getCameras(),
            startDate = startDate,
            expectedCompletionDate = expectedCompletionDate,
            progress = getFloat("progress")
        )
    }
}

