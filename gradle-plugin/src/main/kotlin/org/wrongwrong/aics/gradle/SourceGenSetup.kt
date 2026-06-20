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

    if (project.tasks.findByName(SOURCE_GEN_TASK_NAME) == null) {
        project.tasks.register(SOURCE_GEN_TASK_NAME, SourceGenTask::class.java) { task ->
            task.moduleGroup.set(group)
            task.outputDir.set(generatedDir)
            task.outputs.upToDateWhen { false }
        }
    }

    kotlinCompilation.defaultSourceSet.kotlin.srcDir(generatedDir)

    project.tasks.named(kotlinCompilation.compileKotlinTaskName).configure { compileTask ->
        compileTask.dependsOn(SOURCE_GEN_TASK_NAME)
    }
}
