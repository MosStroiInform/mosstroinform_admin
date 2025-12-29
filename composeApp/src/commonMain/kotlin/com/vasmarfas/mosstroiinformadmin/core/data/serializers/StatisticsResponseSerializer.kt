package com.vasmarfas.mosstroiinformadmin.core.data.serializers

import com.vasmarfas.mosstroiinformadmin.core.data.models.StatisticsResponse
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

object StatisticsResponseSerializer : KSerializer<StatisticsResponse> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("StatisticsResponse") {
        element<Int>("totalProjects")
        element<Int>("availableProjects")
        element<Int>("requestedProjects")
        element<Int>("inProgressProjects")
        element<Int>("totalDocuments")
        element<Int>("pendingDocuments")
        element<Int>("approvedDocuments")
        element<Int>("rejectedDocuments")
        element<Float>("totalRevenue")
        element<Float>("averageProjectPrice")
    }

    override fun serialize(encoder: Encoder, value: StatisticsResponse) {
        val jsonEncoder = encoder as? JsonEncoder ?: return
        jsonEncoder.encodeJsonElement(buildJsonObject {
            put("totalProjects", value.totalProjects)
            put("availableProjects", value.availableProjects)
            put("requestedProjects", value.requestedProjects)
            put("inProgressProjects", value.inProgressProjects)
            put("totalDocuments", value.totalDocuments)
            put("pendingDocuments", value.pendingDocuments)
            put("approvedDocuments", value.approvedDocuments)
            put("rejectedDocuments", value.rejectedDocuments)
            put("totalRevenue", value.totalRevenue)
            put("averageProjectPrice", value.averageProjectPrice)
        })
    }

    override fun deserialize(decoder: Decoder): StatisticsResponse {
        val jsonDecoder = decoder as? JsonDecoder ?: throw IllegalArgumentException("Expected JsonDecoder")
        val json = jsonDecoder.decodeJsonElement().jsonObject
        
        // Логируем все ключи для отладки
        println("StatisticsResponse JSON keys: ${json.keys.joinToString()}")
        
        // Функция для преобразования camelCase в snake_case
        fun camelToSnake(str: String): String {
            return str.replace(Regex("([A-Z])")) { "_${it.groupValues[1].lowercase()}" }
        }
        
        // Пробуем разные варианты имен полей (camelCase, snake_case, lowercase)
        fun getInt(key: String): Int {
            // Пробуем camelCase
            json[key]?.jsonPrimitive?.intOrNull?.let { return it }
            // Пробуем snake_case
            val snakeCase = camelToSnake(key)
            json[snakeCase]?.jsonPrimitive?.intOrNull?.let { return it }
            // Пробуем lowercase
            json[key.lowercase()]?.jsonPrimitive?.intOrNull?.let { return it }
            // Пробуем все ключи в json (регистронезависимый поиск)
            for (jsonKey in json.keys) {
                val jsonKeyLower = jsonKey.lowercase()
                if (jsonKeyLower == key.lowercase() || jsonKeyLower == snakeCase.lowercase()) {
                    return json[jsonKey]?.jsonPrimitive?.intOrNull ?: 0
                }
            }
            return 0
        }
        
        fun getFloat(key: String): Float {
            // Пробуем camelCase
            json[key]?.jsonPrimitive?.floatOrNull?.let { return it }
            // Пробуем snake_case
            val snakeCase = camelToSnake(key)
            json[snakeCase]?.jsonPrimitive?.floatOrNull?.let { return it }
            // Пробуем lowercase
            json[key.lowercase()]?.jsonPrimitive?.floatOrNull?.let { return it }
            // Пробуем все ключи в json (регистронезависимый поиск)
            for (jsonKey in json.keys) {
                val jsonKeyLower = jsonKey.lowercase()
                if (jsonKeyLower == key.lowercase() || jsonKeyLower == snakeCase.lowercase()) {
                    return json[jsonKey]?.jsonPrimitive?.floatOrNull ?: 0f
                }
            }
            return 0f
        }
        
        return StatisticsResponse(
            totalProjects = getInt("totalProjects"),
            availableProjects = getInt("availableProjects"),
            requestedProjects = getInt("requestedProjects"),
            inProgressProjects = getInt("inProgressProjects"),
            totalDocuments = getInt("totalDocuments"),
            pendingDocuments = getInt("pendingDocuments"),
            approvedDocuments = getInt("approvedDocuments"),
            rejectedDocuments = getInt("rejectedDocuments"),
            totalRevenue = getFloat("totalRevenue"),
            averageProjectPrice = getFloat("averageProjectPrice")
        )
    }
}

