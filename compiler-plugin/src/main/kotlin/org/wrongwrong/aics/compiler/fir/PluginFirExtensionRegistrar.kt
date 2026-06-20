package org.wrongwrong.aics.compiler.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar

internal class PluginFirExtensionRegistrar(
    private val group: String,
) : FirExtensionRegistrar() {
    override fun ExtensionRegistrarContext.configurePlugin() {
        +{ session: FirSession -> ContextObjectGenerator(session, group) }
    }
}
