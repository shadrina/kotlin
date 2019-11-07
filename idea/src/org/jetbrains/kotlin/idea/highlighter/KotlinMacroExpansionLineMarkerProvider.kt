/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.highlighter

import com.intellij.codeHighlighting.Pass
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.navigation.GotoRelatedItem
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtQuotation
import org.jetbrains.kotlin.psi.KtReplaceable
import org.jetbrains.kotlin.psi.markHiddenRoot
import java.awt.event.MouseEvent

class KotlinMacroExpansionLineMarkerProvider : RelatedItemLineMarkerProvider() {
    private val KEY = Key<String>("MACRO_EXPANDED_KEY")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<PsiElement>>) {
        if (element !is KtReplaceable || element is KtQuotation) return
        element.expandMarker()?.let { result.add(it) }
        element.undoMarker()?.let { result.add(it) }
    }

    private fun KtReplaceable.expandMarker(): MacroExpandedElementMarkerInfo? {
        if (!hasHiddenElementInitialized) return null
        return MacroExpandedElementMarkerInfo(this, "Expand macro") { _, elt -> expandMacroAnnotation(elt) }
    }

    private fun KtReplaceable.undoMarker(): MacroExpandedElementMarkerInfo? {
        val text = getUserData(KEY) ?: return null
        return MacroExpandedElementMarkerInfo(this, "Undo expansion") { _, elt -> undoMacroExpansion(elt, text) }
    }

    private fun expandMacroAnnotation(element: PsiElement) {
        element as KtReplaceable
        WriteCommandAction.runWriteCommandAction(element.project) {
            val inserted = element.replace(element.hiddenElement) as KtReplaceable
            inserted.putUserData(KEY, element.text)
        }
    }

    private fun undoMacroExpansion(element: PsiElement, text: String) {
        element as KtReplaceable
        WriteCommandAction.runWriteCommandAction(element.project) {
            val inserted = element.replace(element.createHiddenElementFromContent(text))
            (inserted as KtReplaceable).hiddenElement = (element.createHiddenElementFromContent(element.text) as KtReplaceable).apply {
                markHiddenRoot(inserted)
            }
        }
    }

    private inner class MacroExpandedElementMarkerInfo(
        element: PsiElement,
        message: String,
        navHandler: (e: MouseEvent, elt: PsiElement) -> Unit
    ) : RelatedItemLineMarkerInfo<PsiElement>(
        element,
        element.textRange,
        AllIcons.Actions.Expandall,
        Pass.LINE_MARKERS,
        { message },
        navHandler,
        GutterIconRenderer.Alignment.RIGHT,
        listOf<GotoRelatedItem>()
    )
}