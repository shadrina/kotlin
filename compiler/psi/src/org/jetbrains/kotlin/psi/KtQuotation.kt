/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import java.lang.IllegalStateException
import java.lang.StringBuilder

class KtQuotation(node: ASTNode) : KtExpressionImpl(node) {
    companion object {
        private const val QUOTE_TOKEN_OFFSET = 2
        private const val INSERTION_PLACEHOLDER = "x"
    }

    lateinit var realPsi: KtDotQualifiedExpression
    private lateinit var factory: KtPsiFactory
    private val converter = KastreeConverter()

    override fun <R, D> accept(visitor: KtVisitor<R, D>, data: D): R = visitor.visitQuotation(this, data)

    fun initializeRealPsi() {
        if (!::factory.isInitialized) {
            factory = KtPsiFactory(node.psi.project, false)
        }
        val quotationContent = createQuotationContent()
        // TODO: quotation content may be not expression
        val parsed = factory.createExpressionIfPossible(quotationContent)

        try {
            val converted = converter.convertExpr(parsed as KtExpression)
            realPsi = factory.createExpression(converted.toCode()) as KtDotQualifiedExpression

        } catch (e: Exception) {
            when (e) {
                is IllegalStateException, is ClassCastException -> return
                else -> throw e
            }
        }
    }

    fun getEntries() = children.filter { it is KtSimpleNameStringTemplateEntry || it is KtBlockStringTemplateEntry }.toList()

    private fun getEntryContent(entry: PsiElement): String = when (entry) {
        is KtSimpleNameStringTemplateEntry -> entry.firstChild.nextSibling.text
        is KtBlockStringTemplateEntry -> entry.text.removePrefix(entry.firstChild.text).removeSuffix(entry.lastChild.text)
        else -> entry.text
    }

    private fun createQuotationContent(): String {
        val insertionsInfo = mutableMapOf<Int, String>()
        val text = StringBuilder()
        var offset = QUOTE_TOKEN_OFFSET

        for (child in children) {
            val content = getEntryContent(child)
            if (child is KtSimpleNameStringTemplateEntry || child is KtBlockStringTemplateEntry) {
                insertionsInfo.put(child.startOffsetInParent - offset, content)
                text.append(INSERTION_PLACEHOLDER)
                offset += child.text.length - INSERTION_PLACEHOLDER.length
            } else {
                text.append(child.text)
            }
        }
        converter.insertionsInfo = insertionsInfo
        return text.toString()
    }
}
