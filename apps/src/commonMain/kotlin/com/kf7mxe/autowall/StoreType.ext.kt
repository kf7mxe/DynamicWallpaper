package com.kf7mxe.autowall

import com.lightningkite.kiteui.Platform

fun StoreType.toPlatform(): Platform? = when (this) {
    StoreType.Apple -> Platform.iOS
    StoreType.Google -> Platform.Android
    StoreType.Manual -> null
    else -> null
}

fun Platform.toStoreType(): StoreType = when(this){
        Platform.iOS -> StoreType.Apple
        Platform.Android -> StoreType.Google
        else -> StoreType.Manual
    }

