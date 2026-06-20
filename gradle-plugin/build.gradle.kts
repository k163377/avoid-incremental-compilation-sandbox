plugins {
    kotlin("jvm")
    `java-gradle-plugin`
}

dependencies {
    implementation(project(":compiler-plugin"))
    compileOnly(kotlin("gradle-plugin-api"))
    compileOnly(gradleApi())
    testImplementation(kotlin("test"))
}

gradlePlugin {
    plugins {
        create("aics") {
            id = "org.wrongwrong.aics"
            implementationClass = "org.wrongwrong.aics.gradle.GradlePlugin"
        }
    }
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
    }
}

tasks.test {
    useJUnitPlatform()
}
