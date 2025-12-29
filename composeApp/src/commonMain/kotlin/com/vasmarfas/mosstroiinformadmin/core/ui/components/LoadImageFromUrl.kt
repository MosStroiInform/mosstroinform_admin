package com.vasmarfas.mosstroiinformadmin.core.ui.components

import androidx.compose.ui.graphics.ImageBitmap
import io.ktor.client.HttpClient

expect suspend fun loadImageFromUrl(
    url: String,
    httpClient: HttpClient
): ImageBitmap?

