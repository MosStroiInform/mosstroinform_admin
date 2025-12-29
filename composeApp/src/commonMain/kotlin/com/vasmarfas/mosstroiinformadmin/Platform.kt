package com.vasmarfas.mosstroiinformadmin

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform