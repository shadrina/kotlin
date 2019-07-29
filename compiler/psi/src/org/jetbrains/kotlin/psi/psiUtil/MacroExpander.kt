/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi.psiUtil

import com.intellij.openapi.compiler.ex.CompilerPathsEx
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.roots.OrderEnumerator
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import java.lang.reflect.InvocationTargetException
import java.net.URL
import java.net.URLClassLoader
import kotlin.meta.Node

object MacroExpander {
    fun run(annotationEntry: KtAnnotationEntry, node: Node): Node? {
        val modules = ModuleManager.getInstance(annotationEntry.project).modules

        fun Module.outputPaths() = CompilerPathsEx.getOutputPaths(arrayOf(this))
        fun Module.dependenciesPaths() = OrderEnumerator.orderEntries(this).recursively().pathsList.pathList

        val urls = modules
            .fold(mutableListOf<String>(), { acc, m -> acc.also { it.addAll(m.outputPaths() + m.dependenciesPaths()) } })
            .map { URL("file:///$it") }
            .toTypedArray()

        val urlClassLoader = URLClassLoader(urls)
        try {
            val name = annotationEntry.shortName ?: return null
            val klass = urlClassLoader.loadClass(name.identifier) // TODO: How to get fully qualified name?
            val ctor = klass.declaredConstructors[0].also { it.isAccessible = true }
            val applyMethods = klass.declaredMethods.filter { it.name == "apply" }
            // TODO: Overload resolution
            val method = applyMethods.getOrNull(0)?.also { it.isAccessible = true } ?: return null
            val result = method.invoke(ctor.newInstance(), node)
            return null
            // TODO:
            // return result as Node

        } catch (e: ClassNotFoundException) {
            // TODO: Report an error
        } catch (e: InvocationTargetException) {
            // TODO: Report an error
        } catch (e: Exception) {
            // TODO: Report an error
        }
        return null
    }
}
