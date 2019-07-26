/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi.psiUtil

import com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KastreeConverter
import org.jetbrains.kotlin.psi.KtPsiFactory

class MetaTools(node: ASTNode) {
    val factory by lazy { KtPsiFactory(node.psi, false) }
    val converter by lazy { KastreeConverter() }
}
