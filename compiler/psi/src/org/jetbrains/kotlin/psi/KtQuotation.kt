/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.macros.MacroExpander
import org.jetbrains.kotlin.psi.macros.MetaTools
import java.lang.IllegalStateException
import java.lang.StringBuilder
import kotlin.meta.Node

abstract class KtQuotation(node: ASTNode, private val saveIndents: Boolean = true) : KtExpressionImpl(node), KtReplaceable {
    companion object {
        private const val INSERTION_PLACEHOLDER = "x"
    }

    override var metaTools = MetaTools(node)
    override var replacedElement: KtElement = this
    override lateinit var hiddenElement: KtElement
    override var isHidden = false
    override var isRoot = false

    val factory: KtPsiFactory get() = metaTools.factory
    val kastreeConverter: KastreeConverter get() = metaTools.converter

    abstract fun astNodeByContent(content: String): Node

    override fun initializeHiddenElement(macroExpander: MacroExpander) {
        try {
            val converted = astNodeByContent(hiddenElementContent())
            hiddenElement = factory.createExpression(converted.toCode())
            hiddenElement.containingKtFile.analysisContext = containingKtFile

        } catch (t: Throwable) {
            when (t) {
                is IllegalStateException, is ClassCastException, is AssertionError -> return
                else -> throw t
            }
        }
    }

    override val hasHiddenElementInitialized: Boolean get() = ::hiddenElement.isInitialized

    override fun <R, D> accept(visitor: KtVisitor<R, D>, data: D): R = visitor.visitQuotation(this, data)

    fun getEntries(): List<PsiElement> =
        children.filterIsInstance<KtStringTemplateEntryWithExpression>().toList()

    private fun hiddenElementContent(): String {
        val text = StringBuilder()
        var offset = firstChild.textLength
        val insertionsInfo = mutableMapOf<Int, String>()
        if (!saveIndents) {
            val firstChildWithContentText = firstChild.nextSibling.text
            offset += (firstChildWithContentText.length - firstChildWithContentText.trimStart().length)
        }
        for (child in children) {
            child as KtStringTemplateEntry
            val content = child.content()
            val childText = child.text
            when (child) {
                is KtStringTemplateEntryWithExpression -> {
                    insertionsInfo[child.startOffsetInParent - offset] = content
                    text.append(INSERTION_PLACEHOLDER)
                    offset += childText.length - INSERTION_PLACEHOLDER.length
                }
                is KtEscapeStringTemplateEntry -> {
                    text.append(child.unescapedValue)
                    offset++
                }
                else -> text.append(childText)
            }
        }
        kastreeConverter.insertionsInfo = insertionsInfo
        return (if (saveIndents) text else text.trim()).toString()
    }

    private fun KtStringTemplateEntry.content(): String = when (this) {
        is KtSimpleNameStringTemplateEntry -> firstChild.nextSibling.text
        is KtBlockStringTemplateEntry -> text.removePrefix(firstChild.text).removeSuffix(lastChild.text)
        else -> text
    }
}