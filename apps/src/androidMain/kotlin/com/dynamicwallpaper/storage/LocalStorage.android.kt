package com.dynamicwallpaper.storage

import android.annotation.SuppressLint
import android.content.Context
import java.io.File

@SuppressLint("StaticFieldLeak")
object AndroidContext {
    lateinit var appContext: Context
}

actual fun readLocalFile(fileName: String): String? {
    val file = File(AndroidContext.appContext.filesDir, fileName)
    return if (file.exists()) file.readText() else null
}

actual fun writeLocalFile(fileName: String, content: String) {
    val file = File(AndroidContext.appContext.filesDir, fileName)
    file.parentFile?.mkdirs()
    file.writeText(content)
}
