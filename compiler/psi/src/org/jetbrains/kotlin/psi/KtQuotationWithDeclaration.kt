/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi

import com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.psiUtil.startOffset
import kotlin.meta.Node

class KtQuotationWithDeclaration(node: ASTNode) : KtQuotation(node) {
    init {
        converter.offsetGetter = { e -> e.startOffset }
    }

    override fun convertToCustomAST(quotationContent: String): Node {
        // TODO: Explain the choice of generic type
        val parsed = factory.createDeclaration<KtNamedDeclaration>(quotationContent)
        return converter.convertDecl(parsed)
    }

    override fun <R, D> accept(visitor: KtVisitor<R, D>, data: D): R = visitor.visitQuotation(this, data)
}
