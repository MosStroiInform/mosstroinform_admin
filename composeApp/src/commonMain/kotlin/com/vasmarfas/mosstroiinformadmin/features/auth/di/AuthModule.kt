package com.vasmarfas.mosstroiinformadmin.features.auth.di

import com.vasmarfas.mosstroiinformadmin.features.auth.data.AuthApi
import com.vasmarfas.mosstroiinformadmin.features.auth.data.AuthRepository
import com.vasmarfas.mosstroiinformadmin.features.auth.presentation.LoginViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    singleOf(::AuthApi)
    singleOf(::AuthRepository)
    viewModelOf(::LoginViewModel)
}

