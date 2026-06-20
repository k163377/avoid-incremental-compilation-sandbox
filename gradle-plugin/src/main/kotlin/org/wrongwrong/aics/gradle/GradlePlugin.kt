package org.wrongwrong.aics.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class GradlePlugin : KotlinCompilerPluginSupportPlugin {
    override fun apply(target: Project) {
        target.extensions.create("aics", PluginExtension::class.java)
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean =
        kotlinCompilation.compilationName == KotlinCompilation.MAIN_COMPILATION_NAME

    override fun getCompilerPluginId(): String = "org.wrongwrong.aics"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = "org.wrongwrong",
        artifactId = "compiler-plugin",
        version = "1.0-SNAPSHOT",
    )

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>,
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val group = project.group.toString()
        val extension = project.extensions.getByType(PluginExtension::class.java)

        if (extension.generateTargetFunction) {
            setupSourceGen(project, kotlinCompilation, group)
        }

        return project.provider {
            listOf(
                SubpluginOption(key = "group", value = group),
            )
        }
    }
}
