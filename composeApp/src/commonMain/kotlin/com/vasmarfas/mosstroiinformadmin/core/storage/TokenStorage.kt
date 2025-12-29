package com.vasmarfas.mosstroiinformadmin.core.storage

/**
 * Интерфейс для хранилища токенов (для multiplatform)
 */
interface TokenStorage {
    suspend fun saveAccessToken(token: String)
    suspend fun getAccessToken(): String?
    suspend fun saveRefreshToken(token: String)
    suspend fun getRefreshToken(): String?
    suspend fun saveUser(id: String, email: String, name: String, phone: String?)
    suspend fun getUserId(): String?
    suspend fun getUserEmail(): String?
    suspend fun getUserName(): String?
    suspend fun getUserPhone(): String?
    suspend fun isLoggedIn(): Boolean
    suspend fun clear()
}

/**
 * Expect/Actual для создания TokenStorage на разных платформах
 */
expect fun createTokenStorage(): TokenStorage
