package com.vasmarfas.mosstroiinformadmin.core.storage

// WASM пока не имеет полного доступа к Web APIs, используем простую in-memory реализацию
actual fun createTokenStorage(): TokenStorage = InMemoryTokenStorage()

/**
 * In-memory реализация для WASM (пока WASM не поддерживает localStorage)
 */
private class InMemoryTokenStorage : TokenStorage {
    private val storage = mutableMapOf<String, String>()
    
    override suspend fun saveAccessToken(token: String) {
        storage[KEY_ACCESS_TOKEN] = token
    }
    
    override suspend fun getAccessToken(): String? = storage[KEY_ACCESS_TOKEN]
    
    override suspend fun saveRefreshToken(token: String) {
        storage[KEY_REFRESH_TOKEN] = token
    }
    
    override suspend fun getRefreshToken(): String? = storage[KEY_REFRESH_TOKEN]
    
    override suspend fun saveUser(id: String, email: String, name: String, phone: String?) {
        storage[KEY_USER_ID] = id
        storage[KEY_USER_EMAIL] = email
        storage[KEY_USER_NAME] = name
        phone?.let { storage[KEY_USER_PHONE] = it }
    }
    
    override suspend fun getUserId(): String? = storage[KEY_USER_ID]
    override suspend fun getUserEmail(): String? = storage[KEY_USER_EMAIL]
    override suspend fun getUserName(): String? = storage[KEY_USER_NAME]
    override suspend fun getUserPhone(): String? = storage[KEY_USER_PHONE]
    
    override suspend fun isLoggedIn(): Boolean = getAccessToken() != null
    
    override suspend fun clear() {
        storage.clear()
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

