package com.vasmarfas.mosstroiinformadmin.features.documents.di

import com.vasmarfas.mosstroiinformadmin.features.documents.data.DocumentsApi
import com.vasmarfas.mosstroiinformadmin.features.documents.data.DocumentsRepository
import com.vasmarfas.mosstroiinformadmin.features.documents.presentation.DocumentDetailViewModel
import com.vasmarfas.mosstroiinformadmin.features.documents.presentation.DocumentsListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val documentsModule = module {
    single { DocumentsApi(get()) }
    single { DocumentsRepository(get()) }
    viewModel { DocumentsListViewModel(get()) }
    viewModel { params -> DocumentDetailViewModel(params.get(), get()) }
}

