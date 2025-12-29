package com.vasmarfas.mosstroiinformadmin.core.data.serializers

import com.vasmarfas.mosstroiinformadmin.core.data.models.Camera
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

object CameraSerializer : KSerializer<Camera> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Camera") {
        element<String>("id")
        element<String>("name")
        element<String>("description")
        element<String>("streamUrl")
        element<Boolean>("isActive")
        element<String?>("thumbnailUrl", isOptional = true)
    }

    override fun serialize(encoder: Encoder, value: Camera) {
        val jsonEncoder = encoder as? JsonEncoder ?: return
        jsonEncoder.encodeJsonElement(buildJsonObject {
            put("id", value.id)
            put("name", value.name)
            put("description", value.description)
            put("streamUrl", value.streamUrl)
            put("isActive", value.isActive)
            value.thumbnailUrl?.let { put("thumbnailUrl", it) }
        })
    }

    override fun deserialize(decoder: Decoder): Camera {
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
                    return json[jsonKey]?.jsonPrimitive?.content ?: ""
                }
            }
            throw IllegalArgumentException("Missing required field: $key")
        }

        fun getStringOrNull(key: String): String? {
            json[key]?.jsonPrimitive?.contentOrNull?.let { return it }
            val snakeCase = key.replace(Regex("([A-Z])")) { "_${it.groupValues[1].lowercase()}" }
            json[snakeCase]?.jsonPrimitive?.contentOrNull?.let { return it }
            json[key.lowercase()]?.jsonPrimitive?.contentOrNull?.let { return it }
            for (jsonKey in json.keys) {
                val jsonKeyLower = jsonKey.lowercase()
                if (jsonKeyLower == key.lowercase() || jsonKeyLower == snakeCase.lowercase()) {
                    return json[jsonKey]?.jsonPrimitive?.contentOrNull
                }
            }
            return null
        }

        fun getBoolean(key: String): Boolean {
            json[key]?.jsonPrimitive?.booleanOrNull?.let { return it }
            val snakeCase = key.replace(Regex("([A-Z])")) { "_${it.groupValues[1].lowercase()}" }
            json[snakeCase]?.jsonPrimitive?.booleanOrNull?.let { return it }
            json[key.lowercase()]?.jsonPrimitive?.booleanOrNull?.let { return it }
            // Пробуем как строку "true"/"false"
            val stringValue = getStringOrNull(key) ?: getStringOrNull(snakeCase)
            if (stringValue != null) {
                return stringValue.lowercase() == "true"
            }
            return true // По умолчанию
        }

        return Camera(
            id = getString("id"),
            name = getString("name"),
            description = getStringOrNull("description") ?: "",
            streamUrl = getString("streamUrl"),
            isActive = getBoolean("isActive"),
            thumbnailUrl = getStringOrNull("thumbnailUrl")
        )
    }
}

