package com.dynamicwallpaper.sdk

import kotlinx.serialization.Serializable

@Serializable
enum class ApiOption(val apiName: String, val http: String, val ws: String) {
    SameServer("Same Server", "/api", "/api"),
    Local("Local", "http://localhost:8080", "ws://localhost:8080"),
}

expect fun getDefaultServerBackend(): ApiOption
