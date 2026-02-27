package com.dynamicwallpaper

import com.lightningkite.kiteui.models.Icon
import com.lightningkite.kiteui.models.rem

val Icon.Companion.photos
    get() = Icon(
        width = 1.5.rem,
        height = 1.5.rem,
        viewBoxMinX = 0,
        viewBoxMinY = -960,
        viewBoxWidth = 960,
        viewBoxHeight = 960,
        listOf("M360-400h400L622-580l-92 120-62-80-108 140Zm-40 160q-33 0-56.5-23.5T240-320v-480q0-33 23.5-56.5T320-880h480q33 0 56.5 23.5T880-800v480q0 33-23.5 56.5T800-240H320Zm0-80h480v-480H320v480ZM160-80q-33 0-56.5-23.5T80-160v-560h80v560h560v80H160Zm160-720v480-480Z")
    )

val Icon.Companion.photo
    get() = Icon(
        width = 1.5.rem,
        height = 1.5.rem,
        viewBoxMinX = 0,
        viewBoxMinY = -960,
        viewBoxWidth = 960,
        viewBoxHeight = 960,
        listOf("M200-120q-33 0-56.5-23.5T120-200v-560q0-33 23.5-56.5T200-840h560q33 0 56.5 23.5T840-760v560q0 33-23.5 56.5T760-120H200Zm0-80h560v-560H200v560Zm40-80h480L570-480 450-320l-90-120-120 160Zm-40 80v-560 560Z")
    )

