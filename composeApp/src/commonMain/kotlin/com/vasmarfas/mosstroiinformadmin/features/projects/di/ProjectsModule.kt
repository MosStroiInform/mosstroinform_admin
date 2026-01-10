package com.vasmarfas.mosstroiinformadmin.features.projects.di

import com.vasmarfas.mosstroiinformadmin.features.projects.data.ProjectsApi
import com.vasmarfas.mosstroiinformadmin.features.projects.data.ProjectsRepository
import com.vasmarfas.mosstroiinformadmin.features.projects.presentation.ProjectDetailViewModel
import com.vasmarfas.mosstroiinformadmin.features.projects.presentation.ProjectsListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val projectsModule = module {
    single { ProjectsApi(get()) }
    single { ProjectsRepository(get()) }
    viewModel { ProjectsListViewModel(get()) }
    viewModel { params -> ProjectDetailViewModel(params.get(), get(), get(), get()) }
}

