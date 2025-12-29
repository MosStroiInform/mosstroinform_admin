package com.vasmarfas.mosstroiinformadmin.core.di

import com.vasmarfas.mosstroiinformadmin.features.auth.di.authModule
import com.vasmarfas.mosstroiinformadmin.features.chats.di.chatsModule
import com.vasmarfas.mosstroiinformadmin.features.projects.di.projectsModule
import com.vasmarfas.mosstroiinformadmin.features.documents.di.documentsModule
import com.vasmarfas.mosstroiinformadmin.features.construction.di.constructionModule
import com.vasmarfas.mosstroiinformadmin.features.construction_objects.di.constructionObjectsModule
import com.vasmarfas.mosstroiinformadmin.features.completion.di.completionModule
import com.vasmarfas.mosstroiinformadmin.features.admin.di.adminModule
import org.koin.dsl.module

val appModule = module {
    // Core
    includes(coreModule)
    
    // Auth
    includes(authModule)
    
    // Chats
    includes(chatsModule)
    
    // Projects
    includes(projectsModule)
    
    // Documents
    includes(documentsModule)
    
    // Construction Sites
    includes(constructionModule)
    
    // Construction Objects
    includes(constructionObjectsModule)
    
    // Completion
    includes(completionModule)
    
    // Admin
    includes(adminModule)
}

