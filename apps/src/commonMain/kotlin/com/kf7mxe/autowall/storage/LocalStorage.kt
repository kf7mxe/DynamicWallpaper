package com.kf7mxe.autowall.storage

expect fun readLocalFile(fileName: String): String?
expect fun writeLocalFile(fileName: String, content: String)
