package com.vasmarfas.mosstroiinformadmin.features.auth.data

import com.vasmarfas.mosstroiinformadmin.core.data.models.*
import com.vasmarfas.mosstroiinformadmin.core.network.ApiConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class AuthApi(private val client: HttpClient) {
    
    suspend fun login(email: String, password: String): AuthResponse {
        val response = client.post(ApiConfig.Auth.LOGIN) {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }
        
        println("Response status: ${response.status}")
        val bodyText = response.bodyAsText()
        println("Response body: $bodyText")
        
        if (!response.status.isSuccess()) {
            throw Exception("Login failed: ${response.status} - $bodyText")
        }
        
        return response.body()
    }
    
    suspend fun getMe(): User {
        return client.get(ApiConfig.Auth.ME).body()
    }
    
    suspend fun refresh(refreshToken: String): AuthResponse {
        return client.post(ApiConfig.Auth.REFRESH) {
            contentType(ContentType.Application.Json)
            setBody(RefreshRequest(refreshToken))
        }.body()
    }
}

