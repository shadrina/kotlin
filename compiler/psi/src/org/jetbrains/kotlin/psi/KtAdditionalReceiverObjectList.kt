/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi

import com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.stubs.KotlinPlaceHolderStub
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes

class KtAdditionalReceiverObjectList : KtElementImplStub<KotlinPlaceHolderStub<KtAdditionalReceiverObjectList>> {
    constructor(node: ASTNode) : super(node)
    constructor(stub: KotlinPlaceHolderStub<KtAdditionalReceiverObjectList>) : super(
        stub,
        KtStubElementTypes.ADDITIONAL_RECEIVER_OBJECT_LIST
    )

    override fun <R : Any?, D : Any?> accept(visitor: KtVisitor<R, D>, data: D): R {
        return visitor.visitAdditionalReceiverObjectList(this, data)
    }

    fun additionalReceiverObjectExpressions(): List<KtExpression> =
        children.filterIsInstance(KtAdditionalReceiverObject::class.java).mapNotNull { it.expression() }
}