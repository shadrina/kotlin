/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve

import com.google.common.collect.Sets
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.macros.MacroExpander
import org.jetbrains.kotlin.psi.psiUtil.forEachDescendantOfTypeVisitor
import org.jetbrains.kotlin.resolve.BindingContext.PACKAGE_TO_FILES
import org.jetbrains.kotlin.util.slicedMap.WritableSlice
import org.jetbrains.kotlin.diagnostics.Errors.QUOTATION_INITIALIZATION_ERROR
import java.lang.IllegalStateException

interface FilePreprocessorExtension {
    fun preprocessFile(file: KtFile)
}

class FilePreprocessor(
    private val trace: BindingTrace,
    private val extensions: Iterable<FilePreprocessorExtension>
) {
    fun preprocessFile(file: KtFile, macroExpander: MacroExpander) {
        registerFileByPackage(file)

        file.accept(forEachDescendantOfTypeVisitor<KtQuotation> {
            try {
                it.initializeHiddenElement(macroExpander)
            } catch (t: Throwable) {
                when (t) {
                    is IllegalStateException, is ClassCastException, is AssertionError ->
                        trace.report(QUOTATION_INITIALIZATION_ERROR.on(it, t.message ?: ""))
                    else -> throw t
                }
            }
        })
        file.accept(forEachDescendantOfTypeVisitor<KtAnnotated> {
            if (it is KtReplaceable && it.isMacroAnnotated) it.initializeHiddenElement(macroExpander)
        })

        for (extension in extensions) {
            extension.preprocessFile(file)
        }
    }

    private fun registerFileByPackage(file: KtFile) {
        // Register files corresponding to this package
        // The trace currently does not support bi-di multimaps that would handle this task nicer
        trace.addElementToSlice(PACKAGE_TO_FILES, file.packageFqName, file)
    }
}

fun <K, T> BindingTrace.addElementToSlice(
    slice: WritableSlice<K, MutableCollection<T>>, key: K, element: T
) {
    val elements = get(slice, key) ?: Sets.newIdentityHashSet()
    elements.add(element)
    record(slice, key, elements)
}
