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

class KotlinMacroExpansionLineMarkerProvider : RelatedItemLineMarkerProvider() {
    private val KEY = Key<String>("MACRO_EXPANDED_KEY")

    override fun collectNavigationMarkers(element: PsiElement, result: MutableCollection<in RelatedItemLineMarkerInfo<PsiElement>>) {
        if (element !is KtReplaceable || !element.hasHiddenElementInitialized) return
        if (element is KtQuotation) return
        result.add(MacroExpandedElementMarkerInfo(element))
    }

    private fun expandMacroAnnotation(element: PsiElement) {
        if (element !is KtReplaceable) return
        WriteCommandAction.runWriteCommandAction(element.project) {
            val hidden = element.hiddenElement
            hidden.putCopyableUserData(KEY, element.text)
            element.replace(hidden)
        }
    }

    private inner class MacroExpandedElementMarkerInfo(element: PsiElement) : RelatedItemLineMarkerInfo<PsiElement>(
        element,
        element.textRange,
        AllIcons.Actions.Expandall,
        Pass.LINE_MARKERS,
        { "Expand macro" },
        { _, elt -> expandMacroAnnotation(elt) },
        GutterIconRenderer.Alignment.RIGHT,
        listOf<GotoRelatedItem>()
    )
}