/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi

import org.jetbrains.kotlin.psi.psiUtil.MetaTools
import org.jetbrains.kotlin.psi.psiUtil.forEachDescendantOfTypeVisitor

interface KtReplaceable : KtElement {
    var replacedElement: KtElement
    var hiddenElement: KtElement
    var metaTools: MetaTools

    val factory: KtPsiFactory
        get() = metaTools.factory
    val converter: KastreeConverter
        get() = metaTools.converter

    var isHidden: Boolean
    var isRoot: Boolean

    fun initializeHiddenElement()

    fun hasHiddenElementInitialized(): Boolean
}

fun KtReplaceable.markHidden() = accept(forEachDescendantOfTypeVisitor<KtReplaceable> { it.isHidden = true })