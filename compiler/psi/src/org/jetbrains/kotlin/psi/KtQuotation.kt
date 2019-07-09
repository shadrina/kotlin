/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi

import com.intellij.lang.ASTNode
import java.lang.IllegalStateException

class KtQuotation(node: ASTNode) : KtExpressionImpl(node) {
    lateinit var realPsi: KtDotQualifiedExpression
    private lateinit var factory: KtPsiFactory
    private val converter = KastreeConverter()

    fun initializeRealPsi() {
        if (!::factory.isInitialized) {
            factory = KtPsiFactory(node.psi.project, false)
        }
        val quotationContent = node.firstChildNode.treeNext.text
        // TODO: quotation content may be not expression
        val parsed = factory.createExpression(quotationContent)

        try {
            val converted = converter.convertExpr(parsed)
            realPsi = factory.createExpression(converted.toCode()) as KtDotQualifiedExpression

        } catch (_: IllegalStateException) {
            return
        }
    }

    override fun <R, D> accept(visitor: KtVisitor<R, D>, data: D): R = visitor.visitQuotation(this, data)
}
