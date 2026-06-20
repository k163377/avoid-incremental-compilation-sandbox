plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "avoid-incremental-compilation-sandbox"

include(
    ":compiler-plugin",
    ":gradle-plugin",
    ":runtime-api",
    ":integration-test",
    ":integration-test:sample",
)
