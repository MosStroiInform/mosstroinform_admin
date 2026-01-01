package com.vasmarfas.mosstroiinformadmin.features.chats.data

import com.vasmarfas.mosstroiinformadmin.core.data.models.Chat
import com.vasmarfas.mosstroiinformadmin.core.data.models.Message
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class ChatsRepository(
    private val chatsApi: ChatsApi,
    private val webSocketManager: ChatWebSocketManager
) {
    
    suspend fun getChats(): ApiResult<List<Chat>> {
        return try {
            val chats = chatsApi.getChats()
            ApiResult.Success(chats)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка загрузки чатов")
        }
    }
    
    suspend fun getChat(chatId: String): ApiResult<Chat> {
        return try {
            val chat = chatsApi.getChat(chatId)
            ApiResult.Success(chat)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка загрузки чата")
        }
    }
    
    suspend fun getMessages(chatId: String): ApiResult<List<Message>> {
        return try {
            val messages = chatsApi.getMessages(chatId)
            ApiResult.Success(messages)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка загрузки сообщений")
        }
    }
    
    suspend fun sendMessage(chatId: String, text: String): ApiResult<Message> {
        return try {
            val message = chatsApi.sendMessage(chatId, text)
            ApiResult.Success(message)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка отправки сообщения")
        }
    }
    
    suspend fun markAsRead(chatId: String): ApiResult<Unit> {
        return try {
            chatsApi.markAsRead(chatId)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка отметки прочитанных")
        }
    }
    
    // WebSocket
    fun connectToChat(chatId: String): Flow<Message> {
        return webSocketManager.connect(chatId)
            .catch { e -> 
                println("WebSocket connection error: ${e.message}")
            }
    }
    
    suspend fun sendMessageViaWebSocket(text: String) {
        webSocketManager.sendMessage(text, fromSpecialist = true)
    }
    
    suspend fun markAsReadViaWebSocket(messageId: String) {
        webSocketManager.markAsRead(messageId)
    }
    
    fun disconnectFromChat() {
        webSocketManager.disconnect()
    }
}
