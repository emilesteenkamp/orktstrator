import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.maven.publish)
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

publishing {
    publications {
        withType<MavenPublication> {
            groupId = project.rootProject.group.toString()
            version = project.rootProject.version.toString()

            if (name == "kotlinMultiplatform") {
                artifactId = "orktstrator-api"
            }
        }
    }
}