package com.vasmarfas.mosstroiinformadmin.core.utils

import android.content.Intent
import android.net.Uri
import com.vasmarfas.mosstroiinformadmin.core.utils.getApplicationContext
import androidx.core.net.toUri

actual fun openUrlInBrowser(url: String) {
    val context = getApplicationContext()
    val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

