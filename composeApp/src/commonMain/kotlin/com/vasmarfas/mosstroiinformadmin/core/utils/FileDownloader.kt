package com.vasmarfas.mosstroiinformadmin.core.utils

import io.ktor.client.HttpClient

expect suspend fun downloadAndOpenFile(
    url: String,
    fileName: String,
    httpClient: HttpClient
): Result<Unit>

