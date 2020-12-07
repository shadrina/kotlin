/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.checkers

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.types.checker.NewKotlinTypeChecker
import org.jetbrains.kotlin.types.typeUtil.representativeUpperBoundIfTypeParameter
import org.jetbrains.kotlin.types.typeUtil.withTypeParametersErased

class AdditionalReceiverChecker : DeclarationChecker {
    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        if (declaration !is KtPureClassOrObject && declaration !is KtCallableDeclaration) return
        val additionalReceivers = when (descriptor) {
            is CallableDescriptor -> descriptor.additionalReceiverParameters
            is ClassDescriptor -> descriptor.additionalReceivers
            else -> return
        }
        for ((i, receiver) in additionalReceivers.withIndex()) {
            val receiverType = receiver.type.representativeUpperBoundIfTypeParameter().withTypeParametersErased()

            for (another in additionalReceivers.subList(i + 1, additionalReceivers.size)) {
                val anotherReceiverType = another.type.representativeUpperBoundIfTypeParameter().withTypeParametersErased()

                if (NewKotlinTypeChecker.Default.isSubtypeOf(receiverType, anotherReceiverType)
                    || NewKotlinTypeChecker.Default.isSubtypeOf(anotherReceiverType, receiverType)
                ) {
                    context.trace.report(
                        Errors.SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS.on(
                            declaration.firstChild as KtElement,
                            receiverType,
                            anotherReceiverType
                        )
                    )
                    return
                }
            }
        }
    }
}