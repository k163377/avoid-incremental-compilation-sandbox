plugins {
    kotlin("jvm") version "2.3.21"
    id("org.wrongwrong.aics")
}

group = "org.wrongwrong.legacy"
version = "1.0-SNAPSHOT"

// Legacy approach: the targetCall target function is provided by runtime-api and the plugin does not
// generate any source, so unchanged callers are skipped by incremental compilation.
aics {
    generateTargetFunction = false
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.wrongwrong:runtime-api:1.0-SNAPSHOT")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}
