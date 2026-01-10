package com.vasmarfas.mosstroiinformadmin.features.construction_objects.di

import com.vasmarfas.mosstroiinformadmin.features.construction_objects.data.ConstructionObjectsApi
import com.vasmarfas.mosstroiinformadmin.features.construction_objects.data.ConstructionObjectsRepository
import com.vasmarfas.mosstroiinformadmin.features.construction_objects.presentation.ConstructionObjectDetailViewModel
import com.vasmarfas.mosstroiinformadmin.features.construction_objects.presentation.ConstructionObjectsListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val constructionObjectsModule = module {
    single { ConstructionObjectsApi(get()) }
    single { ConstructionObjectsRepository(get()) }
    viewModel { ConstructionObjectsListViewModel(get()) }
    viewModel { params -> ConstructionObjectDetailViewModel(params.get(), get(), get()) }
}

