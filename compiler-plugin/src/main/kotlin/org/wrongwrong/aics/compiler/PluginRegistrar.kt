package org.wrongwrong.aics.compiler

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.wrongwrong.aics.compiler.fir.PluginFirExtensionRegistrar
import org.wrongwrong.aics.compiler.ir.PluginIrGenerationExtension

@ExperimentalCompilerApi
class PluginRegistrar : CompilerPluginRegistrar() {
    override val pluginId: String = PLUGIN_ID
    override val supportsK2: Boolean = true

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val group = configuration.get(KEY_GROUP) ?: error("group is required for this plugin")
        FirExtensionRegistrarAdapter.registerExtension(PluginFirExtensionRegistrar(group))
        IrGenerationExtension.registerExtension(PluginIrGenerationExtension(group))
    }
}
