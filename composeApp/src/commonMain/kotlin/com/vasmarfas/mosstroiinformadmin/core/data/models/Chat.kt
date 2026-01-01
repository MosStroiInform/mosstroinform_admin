package com.vasmarfas.mosstroiinformadmin.core.data.models

import com.vasmarfas.mosstroiinformadmin.core.data.serializers.InstantSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Chat(
    val id: String,
    @SerialName("project_id")
    val projectId: String,
    @SerialName("specialist_name")
    val specialistName: String,
    @SerialName("specialist_avatar_url")
    val specialistAvatarUrl: String? = null,
    @SerialName("last_message")
    val lastMessage: String? = null,
    @SerialName("last_message_at")
    @Serializable(with = InstantSerializer::class)
    val lastMessageAt: Instant? = null,
    @SerialName("unread_count")
    val unreadCount: Int = 0,
    @SerialName("is_active")
    val isActive: Boolean = true
)

@Serializable
data class Message(
    val id: String,
    @SerialName("chat_id")
    val chatId: String? = null, // Может отсутствовать в WebSocket сообщениях
    val text: String,
    @SerialName("sent_at")
    @Serializable(with = InstantSerializer::class)
    val sentAt: Instant,
    @SerialName("is_from_specialist")
    val isFromSpecialist: Boolean = false,
    @SerialName("is_read")
    val isRead: Boolean = false,
    @SerialName("created_at")
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant? = null // Дополнительное поле из WebSocket, игнорируем если нет
)

@Serializable
data class SendMessageRequest(
    val text: String,
    @SerialName("from_specialist")
    val fromSpecialist: Boolean = true
)

@Serializable
data class WebSocketMessage(
    val type: String, // "CREATE" или "READ"
    val text: String? = null,
    val fromSpecialist: Boolean? = null,
    val messageId: String? = null
)
