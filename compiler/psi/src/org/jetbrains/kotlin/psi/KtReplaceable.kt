/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi

import org.jetbrains.kotlin.psi.psiUtil.MetaTools

interface KtReplaceable : KtElement {
    var hiddenElement: KtElement
    var metaTools: MetaTools

    fun initializeHiddenElement()

    // To check if hiddenElement is initialized from java
    fun hasHiddenElementInitialized(): Boolean
}