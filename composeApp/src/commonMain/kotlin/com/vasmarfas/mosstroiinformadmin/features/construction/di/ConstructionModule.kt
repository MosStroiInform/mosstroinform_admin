package com.vasmarfas.mosstroiinformadmin.features.construction.di

import com.vasmarfas.mosstroiinformadmin.features.construction.data.ConstructionSitesApi
import com.vasmarfas.mosstroiinformadmin.features.construction.data.ConstructionSitesRepository
import com.vasmarfas.mosstroiinformadmin.features.construction.presentation.CameraDetailViewModel
import com.vasmarfas.mosstroiinformadmin.features.construction.presentation.SiteDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val constructionModule = module {
    single { ConstructionSitesApi(get()) }
    single { ConstructionSitesRepository(get()) }
    viewModel { params -> SiteDetailViewModel(params.get(), get()) }
    viewModel { params -> 
        val siteId: String = params.get(0)
        val cameraId: String = params.get(1)
        CameraDetailViewModel(siteId, cameraId, get())
    }
}

