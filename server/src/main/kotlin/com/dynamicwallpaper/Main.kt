package com.dynamicwallpaper

import com.lightningkite.kotlinercli.cli
import com.lightningkite.lightningserver.definition.builder.*
import com.lightningkite.lightningserver.engine.ktor.KtorEngine
import com.lightningkite.lightningserver.settings.*
import com.lightningkite.lightningserver.typed.sdk.FetcherSdk
import com.lightningkite.lightningserver.typed.sdk.CachingSdk
import com.lightningkite.lightningserver.typed.sdk.SDK.writeUsingDefaultSettings
import com.lightningkite.lightningserver.typed.sdk.plus
import com.lightningkite.services.data.KFile
import io.ktor.server.netty.Netty
import kotlin.time.TimeSource

private lateinit var settingsFile: KFile

fun setup(settings: KFile = KFile("settings.json")) {
    settingsFile = settings
}

private var engine: KtorEngine? = null

fun engine(setup: KtorEngine.() -> Unit) {
    engine?.let {
        setup(it)
        return
    }

    val before = TimeSource.Monotonic.markNow()
    val built = Server.build()
    println("Server built in ${before.elapsedNow()}")

    engine = KtorEngine(built).apply {
        settings.loadFromFile(settingsFile, internalSerializersModule)
        setup()
    }
}

fun serve() = engine { start(Netty) }

fun sdk() = engine {
    println("Generating FetcherSdk...")
    FetcherSdk("com.dynamicwallpaper.sdk").writeUsingDefaultSettings(
        Server,
        KFile("apps/src/commonMain/kotlin/com/dynamicwallpaper/sdk")
    )
    println("Generating CachingSdk...")
    CachingSdk("com.dynamicwallpaper.sdk").writeUsingDefaultSettings(
        Server,
        KFile("apps/src/commonMain/kotlin/com/dynamicwallpaper/sdk")
    )
    println("SDK generation done.")
}

fun main(vararg args: String) = cli(
    arguments = args,
    setup = ::setup,
    available = listOf(
        ::serve,
        ::sdk,
    ),
    useInteractive = true,
)
