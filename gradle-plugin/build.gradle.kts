plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":compiler-plugin"))
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}
