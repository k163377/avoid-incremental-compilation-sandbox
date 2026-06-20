package org.wrongwrong.aics.gradle

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation

private const val GENERATED_SOURCE_DIR = "build/generated/aics"
private const val SOURCE_GEN_TASK_NAME = "generateAicsSources"

internal fun setupSourceGen(
    project: Project,
    kotlinCompilation: KotlinCompilation<*>,
    group: String,
) {
    val generatedDir = project.layout.projectDirectory.dir(GENERATED_SOURCE_DIR)

    // Snapshot the existing Kotlin source directories (across all compilations, e.g. main and test)
    // before adding the generated one, so the generated output is never tracked as an input of the
    // generation task while still detecting changes to both production and test Kotlin code.
    val kotlinSourceSet = kotlinCompilation.defaultSourceSet.kotlin
    val sourceDirsBeforeGenerated = kotlinCompilation.target.compilations
        .flatMap { compilation -> compilation.defaultSourceSet.kotlin.srcDirs }
        .toSet()

    if (project.tasks.findByName(SOURCE_GEN_TASK_NAME) == null) {
        project.tasks.register(SOURCE_GEN_TASK_NAME, SourceGenTask::class.java) { task ->
            task.moduleGroup.set(group)
            task.outputDir.set(generatedDir)
            // Track only Kotlin (*.kt) sources so the task re-runs (regenerating the marker)
            // exclusively when Kotlin code changes; Java changes do not trigger regeneration.
            task.kotlinSources.from(
                project.files(sourceDirsBeforeGenerated).asFileTree.matching { pattern ->
                    pattern.include("**/*.kt")
                },
            )
        }
    }

    kotlinSourceSet.srcDir(generatedDir)

    project.tasks.named(kotlinCompilation.compileKotlinTaskName).configure { compileTask ->
        compileTask.dependsOn(SOURCE_GEN_TASK_NAME)
    }
}
