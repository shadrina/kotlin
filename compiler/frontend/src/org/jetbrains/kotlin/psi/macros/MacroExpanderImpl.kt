/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi.macros

import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.constants.evaluate.ConstantExpressionEvaluator
import org.jetbrains.kotlin.types.TypeUtils
import java.lang.IllegalArgumentException
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.net.URL
import java.net.URLClassLoader
import kotlin.meta.Node

class MacroExpanderImpl(
    private val trace: BindingTrace,
    private val constantExpressionEvaluator: ConstantExpressionEvaluator,
    dependencies: Collection<String>
) : MacroExpander {
    private val urls: Array<URL> = dependencies
        .map { URL("file:///$it") }
        .toTypedArray()

    override fun run(annotationEntry: KtAnnotationEntry, node: Node): Node? {
        try {
            val name = annotationEntry.fullName() ?: return null
            val classLoader = URLClassLoader(urls, this::class.java.classLoader)
            val klass = classLoader.loadClass(name)
            val args = annotationEntry.valueArguments
                .map {
                    val constant = it.getArgumentExpression()
                        ?.let { e -> constantExpressionEvaluator.evaluateToConstantValue(e, trace, TypeUtils.NO_EXPECTED_TYPE) }
                    constant?.value
                }
                .toTypedArray()
            val instance = klass.declaredConstructors.newInstance(args)
            val invokeMethod = klass.declaredMethods.invokeMethod().apply { isAccessible = true }
            return invokeMethod.invoke(instance, node) as Node

        } catch (e: ClassNotFoundException) {
            // TODO: Report an error
            throw e
        } catch (e: InvocationTargetException) {
            // TODO: Report an error
            throw e
        } catch (e: Exception) {
            // TODO: Report an error
            throw e
        }
    }

    private fun KtAnnotationEntry.fullName(): String? {
        val imports = (containingKtFile.importList?.imports ?: return shortName?.identifier).filter {
            it.importedFqName?.pathSegments()?.last() == shortName
        }
        return when {
            imports.isEmpty() -> shortName?.identifier
            imports.size == 1 -> imports.singleOrNull()?.importedFqName?.asString()
            else -> null
        }
    }

    // We cannot choose constructor according to parameter types because
    // resolving calls is unavailable here
    private fun Array<Constructor<*>>.newInstance(args: Array<Any?>): Any {
        val filtered = this
            .filter { it.parameters.size == args.size }
            .onEach { it.isAccessible = true }
        if (args.isEmpty()) return filtered.single().newInstance()
        var instance: Any
        for (ctor in filtered) {
            try {
                instance = ctor.newInstance(*args)
                return instance
            } catch (e: Exception) {
            }
        }
        throw IllegalArgumentException("No matching constructor found")
    }

    // TODO: Overload resolution
    private fun Array<Method>.invokeMethod(): Method = singleOrNull { it.name == "invoke" }
        ?: throw NoSuchMethodException("No invoke method for macro invocation")
}