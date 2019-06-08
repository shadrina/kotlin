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

    /**
     * Initialization occurs in FilePreprocessor.
     * See [org.jetbrains.kotlin.resolve]
     */
    fun initializeRealPsi() {
        if (!::factory.isInitialized) {
            factory = KtPsiFactory(node.psi.project, false)
        }
        val quotationContext = node.firstChildNode.treeNext.text
        val parsed = factory.createExpressionIfPossible(quotationContext) ?: factory.createFile(quotationContext)

        try {
            val converted = if (parsed is KtExpression) converter.convertExpr(parsed) else converter.convertFile(parsed as KtFile)
            realPsi = factory.createExpression(converted.toCode()) as KtDotQualifiedExpression

        } catch (_: IllegalStateException) {
            return
        }
    }

    override fun <R, D> accept(visitor: KtVisitor<R, D>, data: D): R = visitor.visitQuotation(this, data)
}
