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
        replaceableTools.converter.offsetGetter = { e -> e.startOffset }
    }

    override fun convertToCustomAST(initialContent: String): Node {
        // TODO: Explain the choice of generic type
        val parsed = replaceableTools.factory.createDeclaration<KtNamedDeclaration>(initialContent)
        return replaceableTools.converter.convertDecl(parsed)
    }
}
