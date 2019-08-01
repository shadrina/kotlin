/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi

import com.intellij.lang.ASTNode
import kotlin.meta.Node

class KtQuotationWithType(node: ASTNode) : KtQuotation(node) {
    override fun astNodeByContent(content: String): Node {
        val parsed = factory.createType(content)
        return converter.convertType(parsed)
    }
}
