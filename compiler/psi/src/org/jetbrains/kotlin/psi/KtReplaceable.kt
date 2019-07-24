/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi

import org.jetbrains.kotlin.psi.psiUtil.KtReplaceableTools
import java.lang.IllegalStateException
import kotlin.meta.Node

interface KtReplaceable : KtElement {
    var hiddenPsi: KtElement?
    val replaceableTools: KtReplaceableTools

    fun createHiddenPsiContent(): String

    fun convertToCustomAST(initialContent: String): Node

    fun initializeHiddenPsi()
}