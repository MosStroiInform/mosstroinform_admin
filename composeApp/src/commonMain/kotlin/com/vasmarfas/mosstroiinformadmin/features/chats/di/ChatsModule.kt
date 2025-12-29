package com.vasmarfas.mosstroiinformadmin.features.chats.di

import com.vasmarfas.mosstroiinformadmin.features.chats.data.ChatWebSocketManager
import com.vasmarfas.mosstroiinformadmin.features.chats.data.ChatsApi
import com.vasmarfas.mosstroiinformadmin.features.chats.data.ChatsRepository
import com.vasmarfas.mosstroiinformadmin.features.chats.presentation.ChatListViewModel
import com.vasmarfas.mosstroiinformadmin.features.chats.presentation.ChatDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val chatsModule = module {
    single { ChatsApi(get()) }
    single { ChatWebSocketManager(get()) }
    single { ChatsRepository(get(), get()) }
    viewModel { ChatListViewModel(get()) }
    viewModel { params -> ChatDetailViewModel(params.get(), get()) }
}

