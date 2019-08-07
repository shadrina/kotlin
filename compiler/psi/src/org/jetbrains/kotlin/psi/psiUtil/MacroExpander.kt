/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi.psiUtil

import org.jetbrains.kotlin.psi.KtAnnotationEntry
import java.lang.reflect.InvocationTargetException
import java.net.URL
import java.net.URLClassLoader
import kotlin.meta.Node

object MacroExpander {
    var dependencies: Collection<String> = emptyList()

    fun run(annotationEntry: KtAnnotationEntry, node: Node): Node? {
        val urls = dependencies
            .map { URL("file:///$it") }
            .toTypedArray()
        val urlClassLoader = URLClassLoader(urls, this::class.java.classLoader)
        try {
            val name = annotationEntry.shortName ?: return null
            val klass = urlClassLoader.loadClass(name.identifier) // TODO: How to get fully qualified name?
            val ctor = klass.declaredConstructors[0].also { it.isAccessible = true }
            val applyMethods = klass.declaredMethods.filter { it.name == "invoke" }
            // TODO: Overload resolution
            val method = applyMethods.getOrNull(0)?.also { it.isAccessible = true } ?: return null
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
}
