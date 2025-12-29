package com.vasmarfas.mosstroiinformadmin.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
private val dataStore: DataStore<Preferences> by lazy {
    PreferenceDataStoreFactory.createWithPath(
        produceFile = {
            val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null
            )
            val dataStoreFile = requireNotNull(documentDirectory).path + "/app_preferences.preferences_pb"
            dataStoreFile.toPath()
        }
    )
}

actual fun createTokenStorage(): TokenStorage = DataStoreTokenStorage(dataStore)

private class DataStoreTokenStorage(private val dataStore: DataStore<Preferences>) : TokenStorage {
    
    override suspend fun saveAccessToken(token: String) {
        dataStore.edit { it[KEY_ACCESS_TOKEN] = token }
    }
    
    override suspend fun getAccessToken(): String? {
        return dataStore.data.map { it[KEY_ACCESS_TOKEN] }.first()
    }
    
    override suspend fun saveRefreshToken(token: String) {
        dataStore.edit { it[KEY_REFRESH_TOKEN] = token }
    }
    
    override suspend fun getRefreshToken(): String? {
        return dataStore.data.map { it[KEY_REFRESH_TOKEN] }.first()
    }
    
    override suspend fun saveUser(id: String, email: String, name: String, phone: String?) {
        dataStore.edit { preferences ->
            preferences[KEY_USER_ID] = id
            preferences[KEY_USER_EMAIL] = email
            preferences[KEY_USER_NAME] = name
            phone?.let { preferences[KEY_USER_PHONE] = it }
        }
    }
    
    override suspend fun getUserId(): String? = dataStore.data.map { it[KEY_USER_ID] }.first()
    override suspend fun getUserEmail(): String? = dataStore.data.map { it[KEY_USER_EMAIL] }.first()
    override suspend fun getUserName(): String? = dataStore.data.map { it[KEY_USER_NAME] }.first()
    override suspend fun getUserPhone(): String? = dataStore.data.map { it[KEY_USER_PHONE] }.first()
    
    override suspend fun isLoggedIn(): Boolean = getAccessToken() != null
    
    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_ACCESS_TOKEN)
            preferences.remove(KEY_REFRESH_TOKEN)
            preferences.remove(KEY_USER_ID)
            preferences.remove(KEY_USER_EMAIL)
            preferences.remove(KEY_USER_NAME)
            preferences.remove(KEY_USER_PHONE)
        }
    }
    
    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_USER_EMAIL = stringPreferencesKey("user_email")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_USER_PHONE = stringPreferencesKey("user_phone")
    }
}

