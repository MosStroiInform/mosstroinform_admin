package com.vasmarfas.mosstroiinformadmin.core.storage

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.OptIn

// WASM использует localStorage через JS interop для сохранения токенов между перезагрузками
actual fun createTokenStorage(): TokenStorage = LocalStorageTokenStorage()

// External declaration для localStorage в WASM
@OptIn(ExperimentalWasmJsInterop::class)
private external object LocalStorage {
    fun getItem(key: String): String?
    fun setItem(key: String, value: String)
    fun removeItem(key: String)
}

@OptIn(ExperimentalWasmJsInterop::class)
private val localStorage: LocalStorage = js("localStorage")

/**
 * Реализация на базе browser localStorage для WASM платформы через JS interop
 */
private class LocalStorageTokenStorage : TokenStorage {
    
    override suspend fun saveAccessToken(token: String) {
        localStorage.setItem(KEY_ACCESS_TOKEN, token)
    }
    
    override suspend fun getAccessToken(): String? {
        return localStorage.getItem(KEY_ACCESS_TOKEN)?.takeIf { it.isNotEmpty() }
    }
    
    override suspend fun saveRefreshToken(token: String) {
        localStorage.setItem(KEY_REFRESH_TOKEN, token)
    }
    
    override suspend fun getRefreshToken(): String? {
        return localStorage.getItem(KEY_REFRESH_TOKEN)?.takeIf { it.isNotEmpty() }
    }
    
    override suspend fun saveUser(id: String, email: String, name: String, phone: String?) {
        localStorage.setItem(KEY_USER_ID, id)
        localStorage.setItem(KEY_USER_EMAIL, email)
        localStorage.setItem(KEY_USER_NAME, name)
        phone?.let { localStorage.setItem(KEY_USER_PHONE, it) }
    }
    
    override suspend fun getUserId(): String? = localStorage.getItem(KEY_USER_ID)?.takeIf { it.isNotEmpty() }
    override suspend fun getUserEmail(): String? = localStorage.getItem(KEY_USER_EMAIL)?.takeIf { it.isNotEmpty() }
    override suspend fun getUserName(): String? = localStorage.getItem(KEY_USER_NAME)?.takeIf { it.isNotEmpty() }
    override suspend fun getUserPhone(): String? = localStorage.getItem(KEY_USER_PHONE)?.takeIf { it.isNotEmpty() }
    
    override suspend fun isLoggedIn(): Boolean = getAccessToken() != null
    
    override suspend fun clear() {
        localStorage.removeItem(KEY_ACCESS_TOKEN)
        localStorage.removeItem(KEY_REFRESH_TOKEN)
        localStorage.removeItem(KEY_USER_ID)
        localStorage.removeItem(KEY_USER_EMAIL)
        localStorage.removeItem(KEY_USER_NAME)
        localStorage.removeItem(KEY_USER_PHONE)
    }
    
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_PHONE = "user_phone"
    }
}
