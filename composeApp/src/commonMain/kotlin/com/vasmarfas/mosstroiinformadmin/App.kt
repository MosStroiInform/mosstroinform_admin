package com.vasmarfas.mosstroiinformadmin

import androidx.compose.runtime.*
import com.vasmarfas.mosstroiinformadmin.core.di.appModule
import com.vasmarfas.mosstroiinformadmin.core.di.viewModelModule
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication

@Composable
@Preview
fun App() {
    KoinApplication(
        application = {
            modules(appModule, viewModelModule)
        }
    ) {
        MainApp()
    }
}