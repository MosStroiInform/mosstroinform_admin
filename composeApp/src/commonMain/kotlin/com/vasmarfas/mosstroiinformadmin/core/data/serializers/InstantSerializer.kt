package com.vasmarfas.mosstroiinformadmin.core.data.serializers

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Instant {
        val string = decoder.decodeString()
        return try {
            // Пробуем парсить как ISO 8601 с timezone
            Instant.parse(string)
        } catch (e: Exception) {
            // Если не получилось, пробуем добавить Z (UTC timezone)
            try {
                val stringWithZ = if (string.endsWith("Z") || string.contains("+") || (string.length > 10 && string.substring(10).contains("-"))) {
                    string
                } else {
                    "${string}Z"
                }
                Instant.parse(stringWithZ)
            } catch (e2: Exception) {
                // Если и это не получилось, парсим как LocalDateTime и считаем UTC
                try {
                    // Формат: 2025-12-24T22:31:57.856823
                    val cleanString = string.replace(" ", "T")
                    val parts = cleanString.split("T")
                    if (parts.size == 2) {
                        val datePart = parts[0]
                        val timePart = parts[1].split(".")[0] // Убираем микросекунды если есть
                        val dateTimeString = "${datePart}T${timePart}"
                        kotlinx.datetime.LocalDateTime.parse(dateTimeString)
                            .toInstant(TimeZone.UTC)
                    } else {
                        throw IllegalArgumentException("Invalid date format: $string")
                    }
                } catch (e3: Exception) {
                    throw IllegalArgumentException("Failed to parse Instant from: $string", e3)
                }
            }
        }
    }
}

