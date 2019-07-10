/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi

import com.intellij.lang.ASTNode
import java.lang.IllegalStateException
import java.lang.StringBuilder

class KtQuotation(node: ASTNode) : KtExpressionImpl(node) {
    lateinit var realPsi: KtDotQualifiedExpression
    private lateinit var factory: KtPsiFactory
    private val converter = KastreeConverter()

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

    override fun <R, D> accept(visitor: KtVisitor<R, D>, data: D): R = visitor.visitQuotation(this, data)

    private fun createQuotationContent(): String {
        val externalNames = mutableListOf<String>()
        val text = StringBuilder()
        for (child in children) {
            if (child is KtSimpleNameStringTemplateEntry || child is KtBlockStringTemplateEntry) {
                val content = child.firstChild.nextSibling.text
                externalNames.add(content)
                text.append(content)
            } else {
                text.append(child.text)
            }
        }
        converter.externalNames = externalNames
        return text.toString()
    }
}
