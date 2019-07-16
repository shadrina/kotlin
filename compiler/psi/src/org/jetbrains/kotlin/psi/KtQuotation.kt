/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import java.lang.IllegalStateException
import java.lang.StringBuilder
import kotlin.meta.Node

abstract class KtQuotation(node: ASTNode, private val saveIndents: Boolean = true) : KtExpressionImpl(node) {
    companion object {
        private const val INSERTION_PLACEHOLDER = "x"
    }

    lateinit var realPsi: KtDotQualifiedExpression
    protected lateinit var factory: KtPsiFactory
    protected val converter = KastreeConverter()

    abstract fun convertToCustomAST(quotationContent: String): Node

    fun initializeRealPsi() {
        // TODO: Consider blank line case
        if (!::factory.isInitialized) {
            factory = KtPsiFactory(node.psi.project, false)
        }
        try {
            val quotationContent = createQuotationContent()
            val converted = convertToCustomAST(quotationContent)
            realPsi = factory.createExpression(converted.toCode()) as KtDotQualifiedExpression

        } catch (e: Exception) {
            when (e) {
                is IllegalStateException, is ClassCastException -> return
                else -> throw e
            }
        }
    }

    fun getEntries(): List<PsiElement> =
        children.filter { it is KtSimpleNameStringTemplateEntry || it is KtBlockStringTemplateEntry }.toList()

    private fun createQuotationContent(): String {
        val text = StringBuilder()
        var offset = firstChild.textLength
        val insertionsInfo = mutableMapOf<Int, String>()
        if (!saveIndents) {
            val firstChildWithContentText = firstChild.nextSibling.text
            offset += (firstChildWithContentText.length - firstChildWithContentText.trimStart().length)
        }

        for (child in children) {
            val content = getEntryContent(child)
            val childText = child.text
            if (child is KtSimpleNameStringTemplateEntry || child is KtBlockStringTemplateEntry) {
                insertionsInfo[child.startOffsetInParent - offset] = content
                text.append(INSERTION_PLACEHOLDER)
                offset += childText.length - INSERTION_PLACEHOLDER.length
            } else {
                text.append(childText)
            }
        }
        converter.insertionsInfo = insertionsInfo
        return (if (saveIndents) text else text.trim()).toString()
    }

    private fun getEntryContent(entry: PsiElement): String = when (entry) {
        is KtSimpleNameStringTemplateEntry -> entry.firstChild.nextSibling.text
        is KtBlockStringTemplateEntry -> entry.text.removePrefix(entry.firstChild.text).removeSuffix(entry.lastChild.text)
        else -> entry.text
    }
}