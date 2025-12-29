package com.vasmarfas.mosstroiinformadmin.features.auth.data

import com.vasmarfas.mosstroiinformadmin.core.data.models.User
import com.vasmarfas.mosstroiinformadmin.core.network.ApiResult
import com.vasmarfas.mosstroiinformadmin.core.storage.TokenStorage

class AuthRepository(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) {
    
    suspend fun login(email: String, password: String): ApiResult<User> {
        return try {
            val response = authApi.login(email, password)
            tokenStorage.saveAccessToken(response.accessToken)
            tokenStorage.saveRefreshToken(response.refreshToken)
            tokenStorage.saveUser(
                id = response.user.id,
                email = response.user.email,
                name = response.user.name,
                phone = response.user.phone
            )
            ApiResult.Success(response.user)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка авторизации")
        }
    }
    
    suspend fun getMe(): ApiResult<User> {
        return try {
            val user = authApi.getMe()
            ApiResult.Success(user)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Ошибка получения пользователя")
        }
    }
    
    suspend fun isLoggedIn(): Boolean {
        return tokenStorage.isLoggedIn()
    }
    
    suspend fun getCurrentUser(): User? {
        val id = tokenStorage.getUserId() ?: return null
        val email = tokenStorage.getUserEmail() ?: return null
        val name = tokenStorage.getUserName() ?: return null
        val phone = tokenStorage.getUserPhone()
        
        return User(id, email, name, phone)
    }
    
    suspend fun logout() {
        tokenStorage.clear()
    }
}

