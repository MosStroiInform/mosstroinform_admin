package com.vasmarfas.mosstroiinformadmin.features.completion.di

import com.vasmarfas.mosstroiinformadmin.features.completion.data.CompletionApi
import com.vasmarfas.mosstroiinformadmin.features.completion.data.CompletionRepository
import com.vasmarfas.mosstroiinformadmin.features.completion.presentation.CompletionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val completionModule = module {
    single { CompletionApi(get()) }
    single { CompletionRepository(get()) }
    viewModel { params -> CompletionViewModel(params.get(), get()) }
}

