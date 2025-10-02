import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

dependencies {
    commonMainImplementation(projects.library.api)
    commonMainImplementation(projects.library.core)
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    js(IR) {
        browser()
        nodejs()
    }
    // Apple
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    // Linux
    linuxX64()
    linuxArm64()
    // Windows
    mingwX64()
}

