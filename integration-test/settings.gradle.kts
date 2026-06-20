pluginManagement {
    includeBuild("..")
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "integration-test"

include(
    ":improved-sample",
    ":legacy-sample",
)
