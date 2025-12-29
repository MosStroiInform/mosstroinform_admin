package com.vasmarfas.mosstroiinformadmin.features.admin.di

import com.vasmarfas.mosstroiinformadmin.features.admin.data.AdminApi
import com.vasmarfas.mosstroiinformadmin.features.admin.data.AdminRepository
import com.vasmarfas.mosstroiinformadmin.features.admin.presentation.ProjectCreateEditViewModel
import com.vasmarfas.mosstroiinformadmin.features.admin.presentation.RequestManagementViewModel
import com.vasmarfas.mosstroiinformadmin.features.admin.presentation.StatisticsViewModel
import com.vasmarfas.mosstroiinformadmin.features.admin.presentation.NotificationsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val adminModule = module {
    single<AdminApi> { AdminApi(get()) }
    single<AdminRepository> { AdminRepository(get()) }
    viewModel { RequestManagementViewModel(get()) }
    viewModel { StatisticsViewModel(get()) }
    viewModel { NotificationsViewModel(get()) }
    viewModel { params -> ProjectCreateEditViewModel(get(), params.getOrNull<String>()) }
}

