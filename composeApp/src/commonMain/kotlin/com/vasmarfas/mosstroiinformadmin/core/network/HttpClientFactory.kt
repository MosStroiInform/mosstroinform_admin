package com.vasmarfas.mosstroiinformadmin.core.network

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.milliseconds

object HttpClientFactory {
    
    fun create(tokenProvider: suspend () -> String?): HttpClient {
        return HttpClient {
            expectSuccess = false
            
            // JSON сериализация (оптимизировано для production)
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = false // Отключаем для лучшей производительности
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = false // Отключаем для меньшего размера JSON
                })
            }
            
            // Логирование (отключено для production, можно включить для debug)
            // install(Logging) {
            //     logger = Logger.DEFAULT
            //     level = LogLevel.INFO
            // }
            
            // WebSocket
            install(WebSockets) {
                pingInterval = 20_000.milliseconds
            }
            
            // Таймауты
            install(HttpTimeout) {
                requestTimeoutMillis = ApiConfig.REQUEST_TIMEOUT
                connectTimeoutMillis = ApiConfig.CONNECT_TIMEOUT
                socketTimeoutMillis = ApiConfig.SOCKET_TIMEOUT
            }
            
            // Авторизация
            install(Auth) {
                bearer {
                    loadTokens {
                        tokenProvider()?.let {
                            BearerTokens(accessToken = it, refreshToken = "")
                        }
                    }
                    
                    refreshTokens {
                        tokenProvider()?.let {
                            BearerTokens(accessToken = it, refreshToken = "")
                        }
                    }
                }
            }
            
            // Базовая настройка
            defaultRequest {
                url(ApiConfig.BASE_URL)
            }
        }
    }
}

