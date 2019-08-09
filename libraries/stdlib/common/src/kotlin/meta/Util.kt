/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.meta

fun String.quote(): Node.Expr.Name =
    Node.Expr.Name(value = this)

fun Number.quote(): Node.Expr.Const = when (this) {
    is Double, is Float -> Node.Expr.Const(value = toString(), form = Node.Expr.Const.Form.FLOAT)
    else -> Node.Expr.Const(value = toString(), form = Node.Expr.Const.Form.INT)
}

fun Char.quote(): Node.Expr.Const =
    Node.Expr.Const(value = toString(), form = Node.Expr.Const.Form.CHAR)

fun Boolean.quote(): Node.Expr.Const =
    Node.Expr.Const(value = toString(), form = Node.Expr.Const.Form.BOOLEAN)
