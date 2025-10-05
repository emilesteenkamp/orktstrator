plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.maven.publish)
}

group = "me.emilesteenkamp"
version = "0.0.0-SNAPSHOT-2"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    plugins.withId("maven-publish") {
        publishing {
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/emilesteenkamp/orktstrator")
                    credentials {
                        username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                        password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
                    }
                }
            }
        }
    }
}
