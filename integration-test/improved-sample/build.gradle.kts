plugins {
    kotlin("jvm") version "2.3.21"
    id("org.wrongwrong.aics")
}

group = "org.wrongwrong.sample"
version = "1.0-SNAPSHOT"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}
