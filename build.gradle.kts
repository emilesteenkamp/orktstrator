plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
}

group = "me.emilesteenkamp"
version = "0.0.0"

allprojects {
    repositories {
        mavenCentral()
    }
}
