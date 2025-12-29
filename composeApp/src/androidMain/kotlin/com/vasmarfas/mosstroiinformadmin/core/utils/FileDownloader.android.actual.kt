package com.vasmarfas.mosstroiinformadmin.core.utils

import android.content.Context

object AndroidContextHolder {
    var applicationContext: Context? = null
        private set
    
    fun init(context: Context) {
        applicationContext = context.applicationContext
    }
}

fun getApplicationContext(): Context {
    return AndroidContextHolder.applicationContext
        ?: throw IllegalStateException("Application context not initialized. Call AndroidContextHolder.init() first.")
}

