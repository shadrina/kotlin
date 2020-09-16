/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi2ir.generators

import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.psi.KtExpression

class AdditionalDescriptorStorage {
    private val fieldStorage: MutableMap<KtExpression, PropertyDescriptor> = mutableMapOf()
    private val variableStorage: MutableMap<KtExpression, VariableDescriptor> = mutableMapOf()

    fun put(expression: KtExpression, descriptor: PropertyDescriptor) {
        fieldStorage[expression] = descriptor
    }

    fun put(expression: KtExpression, descriptor: VariableDescriptor) {
        variableStorage[expression] = descriptor
    }

    fun getField(expression: KtExpression) = fieldStorage[expression] ?: error("No field descriptor for receiver $expression")

    fun getVariable(expression: KtExpression) = variableStorage[expression] ?: error("No variable descriptor for receiver $expression")
}