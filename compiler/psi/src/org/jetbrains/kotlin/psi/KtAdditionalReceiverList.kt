/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi

import com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.stubs.KotlinPlaceHolderStub
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes

class KtAdditionalReceiverList : KtElementImplStub<KotlinPlaceHolderStub<KtAdditionalReceiverList>> {
    constructor(node: ASTNode) : super(node)
    constructor(stub: KotlinPlaceHolderStub<KtAdditionalReceiverList>) : super(
        stub,
        KtStubElementTypes.ADDITIONAL_RECEIVER_LIST
    )

    override fun <R : Any?, D : Any?> accept(visitor: KtVisitor<R, D>, data: D): R {
        return visitor.visitAdditionalReceiverList(this, data)
    }

    fun typeReferences(): List<KtTypeReference> =
        children.filterIsInstance(KtAdditionalReceiver::class.java).map { it.typeReference() }
}