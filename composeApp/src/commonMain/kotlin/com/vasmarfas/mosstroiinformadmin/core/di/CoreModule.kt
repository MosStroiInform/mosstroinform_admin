package com.vasmarfas.mosstroiinformadmin.core.di

import com.vasmarfas.mosstroiinformadmin.core.network.HttpClientFactory
import com.vasmarfas.mosstroiinformadmin.core.storage.TokenStorage
import com.vasmarfas.mosstroiinformadmin.core.storage.createTokenStorage
import io.ktor.client.*
import org.koin.dsl.module

val coreModule = module {
    // Token Storage (multiplatform: DataStore для Android/iOS/JVM, localStorage для JS, in-memory для WASM)
    single<TokenStorage> { createTokenStorage() }
    
    // HTTP Client
    single<HttpClient> {
        val tokenStorage: TokenStorage = get()
        HttpClientFactory.create { tokenStorage.getAccessToken() }
    }
}

