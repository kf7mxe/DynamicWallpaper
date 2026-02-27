package com.dynamicwallpaper.storage

import kotlinx.browser.window

actual fun readLocalFile(fileName: String): String? {
    return window.localStorage.getItem(fileName)
}

actual fun writeLocalFile(fileName: String, content: String) {
    window.localStorage.setItem(fileName, content)
}
