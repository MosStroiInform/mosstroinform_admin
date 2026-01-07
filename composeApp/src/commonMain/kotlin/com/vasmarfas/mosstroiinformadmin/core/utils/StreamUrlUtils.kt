package com.vasmarfas.mosstroiinformadmin.core.utils

/**
 * Для non-Android таргетов преобразуем RTSP в HTTPS ссылку,
 * чтобы открывать поток через медиа-сервер (порт 8889).
 *
 * Пример: rtsp://mosstroiinformmedia.vasmarfas.com:8554/vid12
 * ->     https://mosstroiinformmedia.vasmarfas.com:8889/vid12/
 */
fun adjustStreamUrlForNonAndroid(streamUrl: String): String {
    if (!streamUrl.startsWith("rtsp://")) return streamUrl

    val httpsUrl = streamUrl
        .replaceFirst("rtsp://", "https://")
        .replace(":8554", ":8889")

    return if (httpsUrl.endsWith("/")) httpsUrl else "$httpsUrl/"
}

