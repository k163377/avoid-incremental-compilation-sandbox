package org.wrongwrong.aics.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.work.DisableCachingByDefault
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.util.UUID

@DisableCachingByDefault(because = "Generates a new random marker, so the output is not worth caching across builds")
abstract class SourceGenTask : DefaultTask() {
    @get:Input
    abstract val moduleGroup: Property<String>

    /**
     * Kotlin source files (`*.kt`, excluding the generated `TargetCall.kt`) from both production and
     * test compilations that the call sites live in. Tracked so that Gradle's up-to-date checking
     * re-runs this task (regenerating a new marker) only when the Kotlin sources actually change.
     * Java sources are intentionally not tracked.
     */
    @get:InputFiles
    @get:IgnoreEmptyDirectories
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val kotlinSources: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val dir = outputDir.get().asFile
        if (dir.exists()) {
            dir.deleteRecursively()
        }
        dir.mkdirs()

        val packageName = "${moduleGroup.get()}.aics"
        val packagePath = packageName.replace('.', '/')
        val packageDir = dir.resolve(packagePath)
        packageDir.mkdirs()

        val randomValue = UUID.randomUUID().toString()
        val content = buildTargetCallSource(packageName, randomValue)
        packageDir.resolve("TargetCall.kt").writeText(content)
    }

    private fun buildTargetCallSource(packageName: String, randomValue: String): String = """
package $packageName

@Suppress("NOTHING_TO_INLINE")
internal inline fun targetCall(): String {
    val marker = "$randomValue"
    error("targetCall() must be replaced by the compiler plugin. marker=$${"$"}marker")
}
""".trimIndent() + "\n"
}
