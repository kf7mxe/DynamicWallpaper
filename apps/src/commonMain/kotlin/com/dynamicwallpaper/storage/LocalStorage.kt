package com.dynamicwallpaper.storage

expect fun readLocalFile(fileName: String): String?
expect fun writeLocalFile(fileName: String, content: String)
