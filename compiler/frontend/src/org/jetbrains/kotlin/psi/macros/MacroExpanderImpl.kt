/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi.macros

import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.constants.evaluate.ConstantExpressionEvaluator
import java.lang.reflect.InvocationTargetException
import java.net.URL
import java.net.URLClassLoader
import kotlin.meta.Node

class MacroExpanderImpl(
    private val trace: BindingTrace,
    private val constantExpressionEvaluator: ConstantExpressionEvaluator,
    dependencies: Collection<String>
) : MacroExpander {
    private val classLoader: URLClassLoader

    init {
        val urls = dependencies
            .map { URL("file:///$it") }
            .toTypedArray()
        classLoader = URLClassLoader(urls, this::class.java.classLoader)
    }

    override fun run(annotationEntry: KtAnnotationEntry, node: Node): Node? {
        try {
            val name = annotationEntry.fullName() ?: return null
            val klass = classLoader.loadClass(name)
            val ctor = klass.declaredConstructors[0].apply { isAccessible = true }
            val applyMethods = klass.declaredMethods.filter { it.name == "invoke" }
            // TODO: Overload resolution
            val method = applyMethods.getOrNull(0)?.apply { isAccessible = true } ?: return null
            return method.invoke(ctor.newInstance(), node) as Node

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
}