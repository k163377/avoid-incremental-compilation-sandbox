package org.wrongwrong.aics.compiler.ir

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.classId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.wrongwrong.aics.compiler.CONTEXT_OBJECT_NAME
import org.wrongwrong.aics.compiler.GENERATED_PACKAGE_SUFFIX

internal class PluginIrGenerationExtension(
    private val group: String,
) : IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        val contextObjectClassId =
            ClassId(FqName("$group.$GENERATED_PACKAGE_SUFFIX"), Name.identifier(CONTEXT_OBJECT_NAME))
        val contextObject = moduleFragment.files
            .asSequence()
            .flatMap { it.declarations }
            .filterIsInstance<IrClass>()
            .firstOrNull { it.classId == contextObjectClassId }
            ?: return

        val transformer = CallSiteIrTransformer(pluginContext, group, contextObject)
        moduleFragment.transform(transformer, null)
    }
}
