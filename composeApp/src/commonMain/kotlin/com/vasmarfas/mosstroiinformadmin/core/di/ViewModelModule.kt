package com.vasmarfas.mosstroiinformadmin.core.di

import com.vasmarfas.mosstroiinformadmin.features.dashboard.presentation.DashboardViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::DashboardViewModel)
}

