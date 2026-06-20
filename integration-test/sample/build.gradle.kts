plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":runtime-api"))
    implementation(project(":gradle-plugin"))
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}
