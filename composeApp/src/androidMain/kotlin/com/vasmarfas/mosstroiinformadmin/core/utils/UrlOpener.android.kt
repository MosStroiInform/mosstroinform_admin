package com.vasmarfas.mosstroiinformadmin.core.utils

import android.content.Intent
import android.net.Uri
import com.vasmarfas.mosstroiinformadmin.core.utils.getApplicationContext

actual fun openUrlInBrowser(url: String) {
    val context = getApplicationContext()
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

