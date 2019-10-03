/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.macros.MacroExpander
import org.jetbrains.kotlin.psi.macros.MetaTools
import org.jetbrains.kotlin.psi.psiUtil.content
import java.time.LocalDateTime
import kotlin.meta.MutableVisitor
import kotlin.meta.Node

abstract class KtQuotation(node: ASTNode, private val saveIndents: Boolean = true) : KtExpressionImpl(node), KtReplaceable {
    companion object {
        private const val INSERTION_PLACEHOLDER = "x"
    }

    override var replacedElement: KtElement = this
    override lateinit var hiddenElement: KtElement
    override var metaTools = MetaTools(node)
    override var isHidden = false
    override var isRoot = false

    val factory: KtPsiFactory get() = metaTools.factory
    val kastreeConverter: KastreeConverter get() = metaTools.converter
    val offsetToInsertionMapping = mutableMapOf<Int, KtExpression?>()

    abstract fun astNodeByContent(content: String): Node

    override fun initializeHiddenElement(macroExpander: MacroExpander) {
        val converted = astNodeByContent(hiddenElementContent()).also { mapOffsetsToInsertions(it) }
        val finalExpression = factory.createExpression(converted.toCode()) as KtDotQualifiedExpression
        hiddenElement = finalExpression.apply { markHiddenRoot(this@KtQuotation) }
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

    private fun mapOffsetsToInsertions(node: Node) {
        offsetToInsertionMapping.clear()
        val tmpInsertionPlaceholder = generateTemporaryInsertionPlaceholder()
        val tmpInsertionPlaceholderLength = tmpInsertionPlaceholder.length
        val mutated = MutableVisitor.preVisit(node) { v, _ ->
            if (v is Node.Expr.Name && v.isExternal) v.copy(value = tmpInsertionPlaceholder)
            else v
        }
        val entries = getEntries()
        var entryIndex = 0
        var textIndexOffset = 0
        val matches = tmpInsertionPlaceholder.toRegex().findAll(mutated.toCode())
        assert(entries.size == matches.count())
        for (match in matches) {
            val entry = entries[entryIndex++] as KtStringTemplateEntryWithExpression
            val expression = entry.expression
            offsetToInsertionMapping[match.range.first + textIndexOffset] = expression
            val expressionLength = expression?.text?.length ?: 0
            textIndexOffset += expressionLength - tmpInsertionPlaceholderLength
        }
    }

    // TODO: Think how best to create the identifier
    // It must not be present in the code snippet
    private fun generateTemporaryInsertionPlaceholder() = "TEMPORARY_INSERTION_PLACEHOLDER${LocalDateTime.now()}"
}