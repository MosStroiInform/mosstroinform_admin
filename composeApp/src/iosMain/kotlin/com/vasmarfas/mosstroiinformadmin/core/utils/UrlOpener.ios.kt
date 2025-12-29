package com.vasmarfas.mosstroiinformadmin.core.utils

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual fun openUrlInBrowser(url: String) {
    val nsUrl = NSURL(string = url)
    UIApplication.sharedApplication.openURL(nsUrl)
}

