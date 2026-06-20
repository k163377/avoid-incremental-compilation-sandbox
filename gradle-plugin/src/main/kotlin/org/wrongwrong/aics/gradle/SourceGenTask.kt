package org.wrongwrong.aics.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.work.DisableCachingByDefault
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.util.UUID

@DisableCachingByDefault(because = "Always regenerates with a new random value to force inline function recompilation")
abstract class SourceGenTask : DefaultTask() {
    @get:Input
    abstract val moduleGroup: Property<String>

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
