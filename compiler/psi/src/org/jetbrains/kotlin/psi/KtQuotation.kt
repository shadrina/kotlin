/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi

import com.intellij.lang.ASTNode
import java.lang.IllegalStateException

class KtQuotation(node: ASTNode) : KtExpressionImpl(node) {
    lateinit var realPsi: KtDotQualifiedExpression
    private val converter = KastreeConverter()

    /**
     * Initialization occurs in FilePreprocessor.
     * See [org.jetbrains.kotlin.resolve]
     */
    fun initializeRealPsi() {
        val factory = KtPsiFactory(node.psi.project, false)
        val quotationContext = node.firstChildNode.treeNext.text
        val parsed = factory.createExpressionIfPossible(quotationContext) ?: return

        try {
            val stringRepresentation = converter.convertExpr(parsed).toString()
            realPsi = factory.createExpression(stringRepresentation) as KtDotQualifiedExpression

        } catch (_: IllegalStateException) {
            return
        }
    }

    /**
     * Method can be overridden in two ways:
     * 1. Delegate to custom visitQuotation
     * 2. Return only converted PSI if the corresponding property is initialized
     *
     * Second approach is based on the assumption of source node inapplicability
     * after the preprocessing stage.
     */
    override fun <R, D> accept(visitor: KtVisitor<R, D>, data: D): R {
        // 1
        return visitor.visitQuotation(this, data)

        // 2
        // return if (::realPsi.isInitialized) visitor.visitDotQualifiedExpression(realPsi, data)
        // else visitor.visitQuotation(this, data)
    }
}
