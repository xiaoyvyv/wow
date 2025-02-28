@file:Suppress("SpellCheckingInspection")

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "com.xiaoyv"
version = "1.1"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

allprojects {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }

    }
}


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

    implementation("com.github.kwhat:jnativehook:2.2.1")
    implementation("org.jsoup:jsoup:1.15.3")
    implementation("net.java.dev.jna:jna-platform:5.14.0")
    implementation("com.github.kokorin.jaffree:jaffree:2023.09.10")

    implementation("io.github.mymonstercat:rapidocr:+")
    implementation("io.github.mymonstercat:rapidocr-onnx-windows-x86_64:+")
    implementation("io.github.mymonstercat:rapidocr-onnx-macosx-arm64:+")
    implementation("org.slf4j:slf4j-nop:2.0.3")
}

compose.desktop {
    application {
        mainClass = "com.xiaoyv.wow.MainKt"
        jvmArgs += "-Dfile.encoding=UTF-8"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "WowTool"
            packageVersion = "1.0.1"
        }
    }
}
