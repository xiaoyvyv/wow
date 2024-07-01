@file:Suppress("SpellCheckingInspection")

import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "com.xiaoyv"
version = "1.1"

repositories {
    maven("https://maven.aliyun.com/repository/public/")
    maven("https://maven.aliyun.com/repository/central")
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    mavenCentral()
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality
    implementation(compose.desktop.currentOs)

//    implementation("org.bytedeco:javacv:1.5.10")

    // implementation("org.bytedeco:opencv-platform:4.9.0-1.5.10")
//    implementation("org.bytedeco:openblas:0.3.26-1.5.10:windows-x86_64")
//    implementation("org.bytedeco:opencv:4.9.0-1.5.10:windows-x86_64")

    implementation("com.github.kwhat:jnativehook:2.2.1")
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("net.java.dev.jna:jna-platform:5.14.0")
}

compose.desktop {
    application {
        mainClass = "com.xiaoyv.wow.MainKt"
        jvmArgs += "-Dfile.encoding=UTF-8"

        nativeDistributions {
            // targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)

            targetFormats(TargetFormat.Msi)
            packageName = "WowTool"
            packageVersion = "1.0.0"
        }
    }
}
