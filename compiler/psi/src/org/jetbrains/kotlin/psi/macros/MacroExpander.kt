/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi.macros

import org.jetbrains.kotlin.psi.KtAnnotationEntry
import kotlin.meta.Node

interface MacroExpander {
    fun run(annotationEntry: KtAnnotationEntry, node: Node): Node?
}
