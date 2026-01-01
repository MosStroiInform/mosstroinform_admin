package com.vasmarfas.mosstroiinformadmin.features.chats.data

import com.vasmarfas.mosstroiinformadmin.core.data.models.Message
import com.vasmarfas.mosstroiinformadmin.core.data.models.WebSocketMessage
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ChatWebSocketManager(private val client: HttpClient) {
    
    private var session: DefaultClientWebSocketSession? = null
    
    fun connect(chatId: String): Flow<Message> = flow {
        try {
            client.webSocket(
                urlString = "wss://mosstroiinform.vasmarfas.com/chat/$chatId"
            ) {
                session = this
                
                // Слушаем входящие сообщения
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        try {
                            println("WebSocket received: $text")
                            val message = Json.decodeFromString<Message>(text)
                            println("WebSocket parsed message: id=${message.id}, text=${message.text}, isFromSpecialist=${message.isFromSpecialist}")
                            emit(message)
                        } catch (e: Exception) {
                            println("Failed to parse message: $text, error: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("WebSocket error: ${e.message}")
        } finally {
            session = null
        }
    }
    
    suspend fun sendMessage(text: String, fromSpecialist: Boolean = true) {
        if (session == null) {
            println("WebSocket session is null, cannot send message")
            return
        }
        
        val message = WebSocketMessage(
            type = "CREATE",
            text = text,
            fromSpecialist = fromSpecialist // Явно указываем fromSpecialist=true для админки
        )
        
        try {
            session?.send(Frame.Text(Json.encodeToString(message)))
            println("Message sent via WebSocket: text=$text, fromSpecialist=$fromSpecialist")
        } catch (e: Exception) {
            println("Failed to send message: ${e.message}")
            throw e
        }
    }
    
    suspend fun markAsRead(messageId: String) {
        val message = WebSocketMessage(
            type = "READ",
            messageId = messageId
        )
        
        try {
            session?.send(Frame.Text(Json.encodeToString(message)))
        } catch (e: Exception) {
            println("Failed to mark message as read: ${e.message}")
        }
    }
    
    fun disconnect() {
        session = null
    }
}

