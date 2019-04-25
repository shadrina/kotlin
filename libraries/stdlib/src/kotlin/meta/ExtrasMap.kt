/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package kotlin.meta

interface ExtrasMap {
    fun extrasBefore(v: Node): List<Node.Extra>
    fun extrasWithin(v: Node): List<Node.Extra>
    fun extrasAfter(v: Node): List<Node.Extra>

    fun docComment(v: Node): Node.Extra.Comment? {
        for (extra in extrasBefore(v)) if (extra is Node.Extra.Comment && extra.text.startsWith("/**")) return extra
        return null
    }
}