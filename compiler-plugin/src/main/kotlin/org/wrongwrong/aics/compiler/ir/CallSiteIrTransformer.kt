package org.wrongwrong.aics.compiler.ir

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.declarations.addFunction
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irGetObject
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.FqName
import org.wrongwrong.aics.compiler.GENERATED_PACKAGE_SUFFIX
import org.wrongwrong.aics.compiler.TARGET_FUNCTION_NAME

internal class CallSiteIrTransformer(
    private val pluginContext: IrPluginContext,
    group: String,
    private val contextObject: IrClass,
) : IrElementTransformerVoidWithContext() {

    private val runtimeTargetFqn = FqName(TARGET_FUNCTION_NAME)
    private val generatedTargetFqn = FqName("$group.$GENERATED_PACKAGE_SUFFIX.targetCall")

    private val generatedFunctions = mutableMapOf<String, IrSimpleFunction>()

    private var currentCallerFqn: String? = null

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        if (declaration is IrSimpleFunction) {
            currentCallerFqn = declaration.kotlinFqName.asString()
        }
        val result = super.visitFunctionNew(declaration)
        currentCallerFqn = null
        return result
    }

    override fun visitCall(expression: IrCall): IrExpression {
        val calleeFqn = expression.symbol.owner.kotlinFqName
        if (calleeFqn != runtimeTargetFqn && calleeFqn != generatedTargetFqn) {
            return super.visitCall(expression)
        }
        val callerFqn = currentCallerFqn ?: return super.visitCall(expression)

        val accessor = getOrCreateAccessor(contextObject, callerFqn)
        val builder = DeclarationIrBuilder(
            pluginContext,
            expression.symbol,
            expression.startOffset,
            expression.endOffset,
        )
        return IrCallImpl(
            startOffset = expression.startOffset,
            endOffset = expression.endOffset,
            type = accessor.returnType,
            symbol = accessor.symbol,
            typeArgumentsCount = 0,
        ).apply {
            dispatchReceiver = builder.irGetObject(contextObject.symbol)
        }
    }

    private fun getOrCreateAccessor(contextClass: IrClass, callerFqn: String): IrSimpleFunction =
        generatedFunctions.getOrPut(callerFqn) {
            val accessorName = "call_" + callerFqn.hashCode().toUInt().toString(16)
            contextClass.addFunction(
                name = accessorName,
                returnType = pluginContext.irBuiltIns.stringType,
                visibility = DescriptorVisibilities.INTERNAL,
            ).apply {
                body = DeclarationIrBuilder(pluginContext, symbol).irBlockBody {
                    +irReturn(irString("called by $callerFqn"))
                }
            }
        }
}
