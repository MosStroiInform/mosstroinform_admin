package com.vasmarfas.mosstroiinformadmin.features.chats.data

import com.vasmarfas.mosstroiinformadmin.core.data.models.Chat
import com.vasmarfas.mosstroiinformadmin.core.data.models.Message
import com.vasmarfas.mosstroiinformadmin.core.data.models.SendMessageRequest
import com.vasmarfas.mosstroiinformadmin.core.network.ApiConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class ChatsApi(private val client: HttpClient) {
    
    suspend fun getChats(): List<Chat> {
        return client.get(ApiConfig.Chats.LIST).body()
    }
    
    suspend fun getChat(chatId: String): Chat {
        return client.get(ApiConfig.Chats.byId(chatId)).body()
    }
    
    suspend fun getMessages(chatId: String): List<Message> {
        return client.get(ApiConfig.Chats.messages(chatId)).body()
    }
    
    suspend fun sendMessage(chatId: String, text: String): Message {
        return client.post(ApiConfig.Chats.messages(chatId)) {
            contentType(ContentType.Application.Json)
            setBody(SendMessageRequest(text, fromSpecialist = true))
        }.body()
    }
    
    suspend fun markAsRead(chatId: String) {
        client.post(ApiConfig.Chats.markAsRead(chatId))
    }
}
