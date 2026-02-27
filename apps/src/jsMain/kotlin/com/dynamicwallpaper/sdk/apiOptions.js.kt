package com.dynamicwallpaper.sdk

import kotlinx.browser.window

actual fun getDefaultServerBackend(): ApiOption {
    val host = window.location.hostname
    return when {
        host.contains("localhost") -> ApiOption.SameServer
        else -> ApiOption.Local
    }
}
