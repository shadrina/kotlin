/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi

import com.intellij.lang.ASTNode

public class KtQuotation(node: ASTNode) : KtExpressionImpl(node) {
    private lateinit var realPsi: KtDotQualifiedExpression
    private val converter = KastreeConverter()

    public fun initializeRealPsi() {
        val quotationContext = node.firstChildNode.treeNext.text
        val factory = KtPsiFactory(node.psi.project, true)
        val parsed = factory.createFile(quotationContext)
        val stringRepresentation = converter.convertFile(parsed).toString()
        realPsi = factory.createExpression(stringRepresentation) as KtDotQualifiedExpression
    }

    override fun <R, D> accept(visitor: KtVisitor<R, D>, data: D): R =
        if (::realPsi.isInitialized) visitor.visitDotQualifiedExpression(realPsi, data) else visitor.visitQuotation(this, data)
}
