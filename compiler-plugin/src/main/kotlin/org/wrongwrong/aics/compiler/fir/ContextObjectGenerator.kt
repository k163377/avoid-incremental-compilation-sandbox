package org.wrongwrong.aics.compiler.fir

import org.jetbrains.kotlin.GeneratedDeclarationKey
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.plugin.createDefaultPrivateConstructor
import org.jetbrains.kotlin.fir.plugin.createTopLevelClass
import org.jetbrains.kotlin.fir.symbols.impl.FirClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.wrongwrong.aics.compiler.CONTEXT_OBJECT_NAME
import org.wrongwrong.aics.compiler.GENERATED_PACKAGE_SUFFIX

@ExperimentalTopLevelDeclarationsGenerationApi
internal class ContextObjectGenerator(
    session: FirSession,
    group: String,
) : FirDeclarationGenerationExtension(session) {
    internal val generatedPackage: FqName = FqName("$group.$GENERATED_PACKAGE_SUFFIX")
    internal val contextObjectClassId: ClassId = ClassId(generatedPackage, Name.identifier(CONTEXT_OBJECT_NAME))

    override fun generateTopLevelClassLikeDeclaration(classId: ClassId): FirClassLikeSymbol<*>? {
        if (classId != contextObjectClassId) return null
        return createTopLevelClass(classId, Key, ClassKind.OBJECT) {
            visibility = Visibilities.Internal
        }.symbol
    }

    override fun getTopLevelClassIds(): Set<ClassId> = setOf(contextObjectClassId)

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext,
    ): Set<Name> {
        if (classSymbol.classId != contextObjectClassId) return emptySet()
        return setOf(SpecialNames.INIT)
    }

    override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> {
        if (context.owner.classId != contextObjectClassId) return emptyList()
        return listOf(createDefaultPrivateConstructor(context.owner, Key).symbol)
    }

    override fun hasPackage(packageFqName: FqName): Boolean =
        packageFqName == generatedPackage

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {}

    object Key : GeneratedDeclarationKey()
}
