import com.lightningkite.kiteui.KiteUiPluginExtension
import java.nio.file.Files
import java.util.*
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnLockMismatchReport
import org.jetbrains.kotlin.gradle.targets.js.yarn.YarnRootExtension

plugins {
    alias(libs.plugins.androidApp)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.kiteui)
    alias(libs.plugins.kjsplain)
    alias(libs.plugins.kfc)
}

group = "com.dynamicwallpaper"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://jitpack.io")
}

kotlin {
    applyDefaultHierarchyTemplate()
    androidTarget {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    js {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.kiteui)
                api(libs.csvDurable)
                api(libs.lightningServer.core.shared)
                api(libs.lightningServer.typed.shared)
                api(libs.lightningServer.sessions.shared)
                api(libs.lightningServer.client)
                api(project(":shared"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("com.google.android.gms:play-services-location:21.3.0")
            }
        }
        val iosMain by getting {
            dependencies {
            }
        }
        val jsMain by getting {
            dependencies {
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }

    cocoapods {
        version = "1.0"
        summary = "Dynamic Wallpaper KMP App"
        homepage = "https://github.com/dynamicwallpaper"
        ios.deploymentTarget = "14.0"
        name = "apps"

        framework {
            baseName = "apps"
            export(project(":shared"))
            export(libs.kiteui)
            export(libs.lightningServer.client)
        }
    }

    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

android {
    namespace = "com.dynamicwallpaper"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dynamicwallpaper"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    packaging {
        resources.excludes.add("com/lightningkite/lightningserver/lightningdb.txt")
        resources.excludes.add("com/lightningkite/lightningserver/lightningdb-log.txt")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    dependencies {
        coreLibraryDesugaring(libs.desugarJdkLibs)
    }
}

rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin> {
    rootProject.the<YarnRootExtension>().yarnLockMismatchReport =
        YarnLockMismatchReport.WARNING
    rootProject.the<YarnRootExtension>().reportNewYarnLock = true
    rootProject.the<YarnRootExtension>().yarnLockAutoReplace = true
}

configure<KiteUiPluginExtension> {
    this.packageName = "com.dynamicwallpaper"
    this.iosProjectRoot = project.file("./ios/app")
}
