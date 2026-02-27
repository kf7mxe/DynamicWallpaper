package com.dynamicwallpaper.storage

import platform.Foundation.*

actual fun readLocalFile(fileName: String): String? {
    val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
    val documentsDir = paths.firstOrNull() as? String ?: return null
    val filePath = "$documentsDir/$fileName"
    return NSString.stringWithContentsOfFile(filePath, NSUTF8StringEncoding, null)
}

actual fun writeLocalFile(fileName: String, content: String) {
    val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
    val documentsDir = paths.firstOrNull() as? String ?: return
    val filePath = "$documentsDir/$fileName"
    (content as NSString).writeToFile(filePath, atomically = true, encoding = NSUTF8StringEncoding, error = null)
}
