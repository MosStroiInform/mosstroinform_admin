package com.vasmarfas.mosstroiinformadmin.core.data.serializers

import com.vasmarfas.mosstroiinformadmin.core.data.models.Project
import com.vasmarfas.mosstroiinformadmin.core.data.models.ProjectStage
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

object ProjectSerializer : KSerializer<Project> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Project") {
        element<String>("id")
        element<String>("name")
        element<String>("address")
        element<String>("description")
        element<Double>("area")
        element<Int>("floors")
        element<Int>("price")
        element<Int>("bedrooms")
        element<Int>("bathrooms")
        element<String?>("imageUrl")
        element<String>("status")
        element<List<ProjectStage>>("stages")
        element<String?>("objectId")
    }

    override fun serialize(encoder: Encoder, value: Project) {
        val jsonEncoder = encoder as? JsonEncoder ?: return
        jsonEncoder.encodeJsonElement(buildJsonObject {
            put("id", value.id)
            put("name", value.name)
            put("address", value.address)
            put("description", value.description)
            put("area", value.area)
            put("floors", value.floors)
            put("price", value.price)
            put("bedrooms", value.bedrooms)
            put("bathrooms", value.bathrooms)
            value.imageUrl?.let { put("imageUrl", it) }
            put("status", value.status)
            putJsonArray("stages") {
                value.stages.forEach { stage ->
                    addJsonObject {
                        put("id", stage.id)
                        put("name", stage.name)
                        put("status", stage.status)
                    }
                }
            }
            value.objectId?.let { put("objectId", it) }
        })
    }

    override fun deserialize(decoder: Decoder): Project {
        val jsonDecoder = decoder as? JsonDecoder ?: throw IllegalArgumentException("Expected JsonDecoder")
        val json = jsonDecoder.decodeJsonElement().jsonObject
        
        // Логируем все ключи для отладки
        println("Project JSON keys: ${json.keys.joinToString()}")
        
        // Проверяем, не является ли это ошибкой
        if (json.containsKey("detail")) {
            val errorMessage = json["detail"]?.jsonPrimitive?.content ?: "Unknown error"
            println("Backend returned error: $errorMessage")
            throw IllegalArgumentException("Backend error: $errorMessage")
        }
        
        fun getString(key: String): String {
            // Пробуем camelCase
            val camelValue = json[key]?.jsonPrimitive?.contentOrNull
            if (camelValue != null) {
                println("Found $key (camelCase): $camelValue")
                return camelValue
            }
            // Пробуем lowercase
            val lowerValue = json[key.lowercase()]?.jsonPrimitive?.contentOrNull
            if (lowerValue != null) {
                println("Found $key (lowercase): $lowerValue")
                return lowerValue
            }
            // Пробуем snake_case
            val snakeCase = key.replace(Regex("([A-Z])")) { "_${it.groupValues[1].lowercase()}" }
            val snakeValue = json[snakeCase]?.jsonPrimitive?.contentOrNull
            if (snakeValue != null) {
                println("Found $key (snake_case): $snakeValue")
                return snakeValue
            }
            // Пробуем все ключи в json (регистронезависимый поиск)
            for (jsonKey in json.keys) {
                val jsonKeyLower = jsonKey.lowercase()
                if (jsonKeyLower == key.lowercase() || jsonKeyLower == snakeCase.lowercase()) {
                    val jsonElement = json[jsonKey]
                    // UUID может быть примитивом (строкой) или объектом
                    val value = when {
                        jsonElement is JsonPrimitive -> jsonElement.contentOrNull
                        else -> jsonElement.toString().trim('"')
                    }
                    if (value != null && value.isNotBlank()) {
                        println("Found $key (matched $jsonKey): $value")
                        return value
                    }
                }
            }
            println("Missing required field: $key (tried: $key, ${key.lowercase()}, $snakeCase)")
            println("Available keys: ${json.keys.joinToString()}")
            println("Full JSON: ${json.toString()}")
            throw IllegalArgumentException("Missing required field: $key")
        }
        
        fun getStringOrNull(key: String): String? {
            json[key]?.jsonPrimitive?.contentOrNull?.let { return it }
            json[key.lowercase()]?.jsonPrimitive?.contentOrNull?.let { return it }
            val snakeCase = key.replace(Regex("([A-Z])")) { "_${it.groupValues[1].lowercase()}" }
            json[snakeCase]?.jsonPrimitive?.contentOrNull?.let { return it }
            return null
        }
        
        fun getDouble(key: String): Double {
            json[key]?.jsonPrimitive?.doubleOrNull?.let { return it }
            json[key.lowercase()]?.jsonPrimitive?.doubleOrNull?.let { return it }
            val snakeCase = key.replace(Regex("([A-Z])")) { "_${it.groupValues[1].lowercase()}" }
            json[snakeCase]?.jsonPrimitive?.doubleOrNull?.let { return it }
            throw IllegalArgumentException("Missing required field: $key")
        }
        
        fun getInt(key: String): Int {
            json[key]?.jsonPrimitive?.intOrNull?.let { return it }
            json[key.lowercase()]?.jsonPrimitive?.intOrNull?.let { return it }
            val snakeCase = key.replace(Regex("([A-Z])")) { "_${it.groupValues[1].lowercase()}" }
            json[snakeCase]?.jsonPrimitive?.intOrNull?.let { return it }
            throw IllegalArgumentException("Missing required field: $key")
        }
        
        fun getStages(): List<ProjectStage> {
            val stagesJson = json["stages"]?.jsonArray
            if (stagesJson == null) return emptyList()
            
            return stagesJson.map { stageJson ->
                val stageObj = stageJson.jsonObject
                ProjectStage(
                    id = stageObj["id"]?.jsonPrimitive?.content ?: "",
                    name = stageObj["name"]?.jsonPrimitive?.content ?: "",
                    status = stageObj["status"]?.jsonPrimitive?.content ?: ""
                )
            }
        }
        
        return Project(
            id = getString("id"),
            name = getString("name"),
            address = getString("address"),
            description = getStringOrNull("description") ?: "",
            area = getDouble("area"),
            floors = getInt("floors"),
            price = getInt("price"),
            bedrooms = getInt("bedrooms"),
            bathrooms = getInt("bathrooms"),
            imageUrl = getStringOrNull("imageUrl") ?: getStringOrNull("image_url"),
            status = getString("status"),
            stages = getStages(),
            objectId = getStringOrNull("objectId") ?: getStringOrNull("object_id")
        )
    }
}

