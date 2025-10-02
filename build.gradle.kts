plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
}

group = "me.emilesteenkamp"

allprojects {
    repositories {
        mavenCentral()
    }
}
