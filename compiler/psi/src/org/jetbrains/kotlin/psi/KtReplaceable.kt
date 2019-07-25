/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi

import org.jetbrains.kotlin.psi.psiUtil.ReplaceableTools
import kotlin.meta.Node

interface KtReplaceable : KtElement {
    var hiddenElement: KtElement?
    val replaceableTools: ReplaceableTools?

    fun hiddenElementContent(): String

    fun astNodeByContent(content: String): Node

    fun initializeHiddenElement()
}