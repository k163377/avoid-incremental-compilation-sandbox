plugins {
    kotlin("jvm") version "2.3.21" apply false
}

group = "org.wrongwrong"
version = "1.0-SNAPSHOT"

subprojects {
    group = rootProject.group
    version = rootProject.version
    repositories {
        mavenCentral()
    }
}
