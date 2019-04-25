/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package kotlin.meta

sealed class Node {
    companion object {
        private const val PREFIX = "kotlin.meta.Node."

        enum class ArgType { VALUE, LIST }
        data class ArgDesc(val name: String, val value: String, val type: ArgType)

        fun stringRepresentation(className: String, args: List<ArgDesc>): String {
            fun arrayToListOf(arrayString: String) = "listOf(${arrayString.substring(1, arrayString.length - 1)})"
            return "$PREFIX$className(${
                args.map { when (it.type) {
                    Companion.ArgType.VALUE -> "${it.name}=${it.value}"
                    Companion.ArgType.LIST  -> "${it.name}=${arrayToListOf(it.value)}"
                }}.joinToString(", ")
            })"
        }
    }

    var tag: Any? = null

    interface WithAnnotations {
        val anns: List<Modifier.AnnotationSet>
    }

    interface WithModifiers : WithAnnotations {
        val mods: List<Modifier>
        override val anns: List<Modifier.AnnotationSet> get() = mods.mapNotNull { it as? Modifier.AnnotationSet }
    }

    interface Entry : WithAnnotations {
        val pkg: Package?
        val imports: List<Import>
    }

    data class File(
        override val anns: List<Modifier.AnnotationSet>,
        override val pkg: Package?,
        override val imports: List<Import>,
        val decls: List<Decl>
    ) : Node(), Entry {
        override fun toString() = stringRepresentation("File", listOf(
            ArgDesc("anns", anns.toString(), Companion.ArgType.LIST),
            ArgDesc("pkg", pkg.toString(), Companion.ArgType.VALUE),
            ArgDesc("imports", imports.toString(), Companion.ArgType.LIST),
            ArgDesc("decls", decls.toString(), Companion.ArgType.LIST)
        ))
    }

    data class Script(
        override val anns: List<Modifier.AnnotationSet>,
        override val pkg: Package?,
        override val imports: List<Import>,
        val exprs: List<Expr>
    ) : Node(), Entry {
        override fun toString() = stringRepresentation("Script", listOf(
            ArgDesc("anns", anns.toString(), Companion.ArgType.LIST),
            ArgDesc("pkg", pkg.toString(), Companion.ArgType.VALUE),
            ArgDesc("imports", imports.toString(), Companion.ArgType.LIST),
            ArgDesc("exprs", exprs.toString(), Companion.ArgType.LIST)
        ))
    }

    data class Package(
        override val mods: List<Modifier>,
        val names: List<String>
    ) : Node(), WithModifiers {
        override fun toString() = stringRepresentation("Package", listOf(
            ArgDesc("mods", mods.toString(), Companion.ArgType.LIST),
            ArgDesc("names", names.toString(), Companion.ArgType.LIST)
        ))
    }

    data class Import(
        val names: List<String>,
        val wildcard: Boolean,
        val alias: String?
    ) : Node() {
        override fun toString() = stringRepresentation("Import", listOf(
            ArgDesc("names", names.toString(), Companion.ArgType.LIST),
            ArgDesc("wildcard", wildcard.toString(), Companion.ArgType.VALUE),
            ArgDesc("alias", alias.toString(), Companion.ArgType.VALUE)
        ))
    }

    sealed class Decl : Node() {
        data class Structured(
            override val mods: List<Modifier>,
            val form: Form,
            val name: String,
            val typeParams: List<TypeParam>,
            val primaryConstructor: PrimaryConstructor?,
            val parentAnns: List<Modifier.AnnotationSet>,
            val parents: List<Parent>,
            val typeConstraints: List<TypeConstraint>,
            // TODO: Can include primary constructor
            val members: List<Decl>
        ) : Decl(), WithModifiers {
            override fun toString() = stringRepresentation("Decl.Structured", listOf(
                ArgDesc("mods", mods.toString(), Companion.ArgType.LIST),
                ArgDesc("form", form.toString(), Companion.ArgType.VALUE),
                ArgDesc("name", name, Companion.ArgType.VALUE),
                ArgDesc("typeParams", typeParams.toString(), Companion.ArgType.LIST),
                ArgDesc("primaryConstructor", primaryConstructor.toString(), Companion.ArgType.VALUE),
                ArgDesc("parentAnns", parentAnns.toString(), Companion.ArgType.LIST),
                ArgDesc("parents", parents.toString(), Companion.ArgType.LIST),
                ArgDesc("typeConstraints", typeConstraints.toString(), Companion.ArgType.LIST),
                ArgDesc("members", members.toString(), Companion.ArgType.LIST)
            ))
            enum class Form {
                CLASS, ENUM_CLASS, INTERFACE, OBJECT, COMPANION_OBJECT
            }
            sealed class Parent : Node() {
                data class CallConstructor(
                    val type: TypeRef.Simple,
                    val typeArgs: List<Node.Type?>,
                    val args: List<ValueArg>,
                    val lambda: Expr.Call.TrailLambda?
                ) : Parent() {
                    override fun toString() = stringRepresentation("Decl.Structured.Parent.CallConstructor", listOf(
                        ArgDesc("type", type.toString(), Companion.ArgType.VALUE),
                        ArgDesc("typeArgs", typeArgs.toString(), Companion.ArgType.LIST),
                        ArgDesc("args", args.toString(), Companion.ArgType.LIST),
                        ArgDesc("lambda", lambda.toString(), Companion.ArgType.VALUE)
                    ))
                }
                data class Type(
                    val type: TypeRef.Simple,
                    val by: Expr?
                ) : Parent() {
                    override fun toString() = stringRepresentation("Decl.Structured.Parent.Type", listOf(
                        ArgDesc("type", type.toString(), Companion.ArgType.VALUE),
                        ArgDesc("by", by.toString(), Companion.ArgType.VALUE)
                    ))
                }
            }
            data class PrimaryConstructor(
                override val mods: List<Modifier>,
                val params: List<Func.Param>
            ) : Node(), WithModifiers {
                override fun toString() = stringRepresentation("Decl.Structured.PrimaryConstructor", listOf(
                    ArgDesc("mods", mods.toString(), Companion.ArgType.LIST),
                    ArgDesc("params", params.toString(), Companion.ArgType.LIST)
                ))
            }
        }
        data class Init(val block: Block) : Decl() {
            override fun toString() = stringRepresentation("Decl.Init", listOf(
                ArgDesc("block", block.toString(), Companion.ArgType.VALUE)
            ))
        }
        data class Func(
            override val mods: List<Modifier>,
            val typeParams: List<TypeParam>,
            val receiverType: Type?,
            // Name not present on anonymous functions
            val name: String?,
            val paramTypeParams: List<TypeParam>,
            val params: List<Param>,
            val type: Type?,
            val typeConstraints: List<TypeConstraint>,
            val body: Body?
        ) : Decl(), WithModifiers {
            override fun toString() = stringRepresentation("Decl.Func", listOf(
                ArgDesc("mods", mods.toString(), Companion.ArgType.LIST),
                ArgDesc("typeParams", typeParams.toString(), Companion.ArgType.LIST),
                ArgDesc("receiverType", receiverType.toString(), Companion.ArgType.VALUE),
                ArgDesc("name", name.toString(), Companion.ArgType.VALUE),
                ArgDesc("paramTypeParams", paramTypeParams.toString(), Companion.ArgType.LIST),
                ArgDesc("params", params.toString(), Companion.ArgType.LIST),
                ArgDesc("type", type.toString(), Companion.ArgType.VALUE),
                ArgDesc("typeConstraints", typeConstraints.toString(), Companion.ArgType.LIST),
                ArgDesc("body", body.toString(), Companion.ArgType.VALUE)
            ))
            data class Param(
                override val mods: List<Modifier>,
                val readOnly: Boolean?,
                val name: String,
                // Type can be null for anon functions
                val type: Type?,
                val default: Expr?
            ) : Node(), WithModifiers {
                override fun toString() = stringRepresentation("Decl.Func.Param", listOf(
                    ArgDesc("mods", mods.toString(), Companion.ArgType.LIST),
                    ArgDesc("readOnly", readOnly.toString(), Companion.ArgType.VALUE),
                    ArgDesc("name", name, Companion.ArgType.VALUE),
                    ArgDesc("type", type.toString(), Companion.ArgType.VALUE),
                    ArgDesc("default", default.toString(), Companion.ArgType.VALUE)
                ))
            }
            sealed class Body : Node() {
                data class Block(val block: Node.Block) : Body() {
                    override fun toString() = stringRepresentation("Decl.Func.Body", listOf(
                        ArgDesc("block", block.toString(), Companion.ArgType.VALUE)
                    ))
                }
                data class Expr(val expr: Node.Expr) : Body() {
                    override fun toString() = stringRepresentation("Decl.Func.Expr", listOf(
                        ArgDesc("expr", expr.toString(), Companion.ArgType.VALUE)
                    ))
                }
            }
        }
        data class Property(
            override val mods: List<Modifier>,
            val readOnly: Boolean,
            val typeParams: List<TypeParam>,
            val receiverType: Type?,
            // Always at least one, more than one is destructuring, null is underscore in destructure
            val vars: List<Var?>,
            val typeConstraints: List<TypeConstraint>,
            val delegated: Boolean,
            val expr: Expr?,
            val accessors: Accessors?
        ) : Decl(), WithModifiers {
            override fun toString() = stringRepresentation("Decl.Property", listOf(
                ArgDesc("mods", mods.toString(), Companion.ArgType.LIST),
                ArgDesc("readOnly", readOnly.toString(), Companion.ArgType.VALUE),
                ArgDesc("typeParams", typeParams.toString(), Companion.ArgType.LIST),
                ArgDesc("receiverType", receiverType.toString(), Companion.ArgType.VALUE),
                ArgDesc("vars", vars.toString(), Companion.ArgType.LIST),
                ArgDesc("typeConstraints", typeConstraints.toString(), Companion.ArgType.LIST),
                ArgDesc("delegated", delegated.toString(), Companion.ArgType.VALUE),
                ArgDesc("expr", expr.toString(), Companion.ArgType.VALUE),
                ArgDesc("accessors", accessors.toString(), Companion.ArgType.LIST)
            ))
            data class Var(
                val name: String,
                val type: Type?
            ) : Node() {
                override fun toString() = stringRepresentation("Decl.Property.Var", listOf(
                    ArgDesc("name", name, Companion.ArgType.VALUE),
                    ArgDesc("type", type.toString(), Companion.ArgType.VALUE)
                ))
            }
            data class Accessors(
                val first: Accessor,
                val second: Accessor?
            ) : Node() {
                override fun toString() = stringRepresentation("Decl.Property.Accessors", listOf(
                    ArgDesc("first", first.toString(), Companion.ArgType.VALUE),
                    ArgDesc("second", second.toString(), Companion.ArgType.VALUE)
                ))
            }
            sealed class Accessor : Node(), WithModifiers {
                data class Get(
                    override val mods: List<Modifier>,
                    val type: Type?,
                    val body: Func.Body?
                ) : Accessor() {
                    override fun toString() = stringRepresentation("Decl.Property.Accessor.Get", listOf(
                        ArgDesc("mods", mods.toString(), Companion.ArgType.LIST),
                        ArgDesc("type", type.toString(), Companion.ArgType.VALUE),
                        ArgDesc("body", body.toString(), Companion.ArgType.VALUE)
                    ))
                }
                data class Set(
                    override val mods: List<Modifier>,
                    val paramMods: List<Modifier>,
                    val paramName: String?,
                    val paramType: Type?,
                    val body: Func.Body?
                ) : Accessor() {
                    override fun toString() = stringRepresentation("Decl.Property.Accessor.Set", listOf(
                        ArgDesc("mods", mods.toString(), Companion.ArgType.LIST),
                        ArgDesc("paramMods", paramMods.toString(), Companion.ArgType.LIST),
                        ArgDesc("paramType", paramType.toString(), Companion.ArgType.VALUE),
                        ArgDesc("body", body.toString(), Companion.ArgType.VALUE)
                    ))
                }
            }
        }
        data class TypeAlias(
            override val mods: List<Modifier>,
            val name: String,
            val typeParams: List<TypeParam>,
            val type: Type
        ) : Decl(), WithModifiers {
            override fun toString() = stringRepresentation("Decl.TypeAlias", listOf(
                ArgDesc("mods", mods.toString(), Companion.ArgType.LIST),
                ArgDesc("name", name, Companion.ArgType.VALUE),
                ArgDesc("typeParams", typeParams.toString(), Companion.ArgType.LIST),
                ArgDesc("type", type.toString(), Companion.ArgType.VALUE)
            ))
        }
        data class Constructor(
            override val mods: List<Modifier>,
            val params: List<Func.Param>,
            val delegationCall: DelegationCall?,
            val block: Block?
        ) : Decl(), WithModifiers {
            override fun toString() = stringRepresentation("Decl.Constructor", listOf(
                ArgDesc("mods", mods.toString(), Companion.ArgType.LIST),
                ArgDesc("params", params.toString(), Companion.ArgType.LIST),
                ArgDesc("delegationCall", delegationCall.toString(), Companion.ArgType.VALUE),
                ArgDesc("block", block.toString(), Companion.ArgType.VALUE)
            ))
            data class DelegationCall(
                val target: DelegationTarget,
                val args: List<ValueArg>
            ) : Node() {
                override fun toString() = stringRepresentation("Decl.Constructor.DelegationCall", listOf(
                    ArgDesc("target", target.toString(), Companion.ArgType.VALUE),
                    ArgDesc("args", args.toString(), Companion.ArgType.LIST)
                ))
            }
            enum class DelegationTarget { THIS, SUPER }
        }
        data class EnumEntry(
            override val mods: List<Modifier>,
            val name: String,
            val args: List<ValueArg>,
            val members: List<Decl>
        ) : Decl(), WithModifiers {
            override fun toString() = stringRepresentation("Decl.EnumEntry", listOf(
                ArgDesc("mods", mods.toString(), Companion.ArgType.LIST),
                ArgDesc("name", name, Companion.ArgType.VALUE),
                ArgDesc("args", args.toString(), Companion.ArgType.LIST),
                ArgDesc("members", members.toString(), Companion.ArgType.LIST)
            ))
        }
    }

    data class TypeParam(
        override val mods: List<Modifier>,
        val name: String,
        val type: TypeRef?
    ) : Node(), WithModifiers {
        override fun toString() = stringRepresentation("TypeParam", listOf(
            ArgDesc("mods", mods.toString(), Companion.ArgType.LIST),
            ArgDesc("name", name, Companion.ArgType.VALUE),
            ArgDesc("type", type.toString(), Companion.ArgType.VALUE)
        ))
    }

    data class TypeConstraint(
        override val anns: List<Modifier.AnnotationSet>,
        val name: String,
        val type: Type
    ) : Node(), WithAnnotations {
        override fun toString() = stringRepresentation("TypeConstraint", listOf(
            ArgDesc("anns", anns.toString(), Companion.ArgType.LIST),
            ArgDesc("name", name, Companion.ArgType.VALUE),
            ArgDesc("type", type.toString(), Companion.ArgType.VALUE)
        ))
    }

    sealed class TypeRef : Node() {
        data class Paren(
            override val mods: List<Modifier>,
            val type: TypeRef
        ) : TypeRef(), WithModifiers {
            override fun toString() = stringRepresentation("TypeRef.Paren", listOf(
                ArgDesc("mods", mods.toString(), Companion.ArgType.LIST),
                ArgDesc("type", type.toString(), Companion.ArgType.VALUE)
            ))
        }
        data class Func(
            val receiverType: Type?,
            val params: List<Param>,
            val type: Type
        ) : TypeRef() {
            override fun toString() = stringRepresentation("TypeRef.Func", listOf(
                ArgDesc("receiverType", receiverType.toString(), Companion.ArgType.VALUE),
                ArgDesc("params", params.toString(), Companion.ArgType.LIST),
                ArgDesc("type", type.toString(), Companion.ArgType.VALUE)
            ))
            data class Param(
                val name: String?,
                val type: Type
            ) : Node() {
                override fun toString() = stringRepresentation("TypeRef.Func.Param", listOf(
                    ArgDesc("name", name.toString(), Companion.ArgType.VALUE),
                    ArgDesc("type", type.toString(), Companion.ArgType.VALUE)
                ))
            }
        }
        data class Simple(
            val pieces: List<Piece>
        ) : TypeRef() {
            override fun toString() = stringRepresentation("TypeRef.Simple", listOf(
                ArgDesc("pieces", pieces.toString(), Companion.ArgType.LIST)
            ))
            data class Piece(
                val name: String,
                // Null means any
                val typeParams: List<Type?>
            ) : Node() {
                override fun toString() = stringRepresentation("TypeRef.Simple.Piece", listOf(
                    ArgDesc("name", name, Companion.ArgType.VALUE),
                    ArgDesc("typeParams", typeParams.toString(), Companion.ArgType.LIST)
                ))
            }
        }
        data class Nullable(val type: TypeRef) : TypeRef() {
            override fun toString() = stringRepresentation("TypeRef.Nullable", listOf(
                ArgDesc("type", type.toString(), Companion.ArgType.VALUE)
            ))
        }
        data class Dynamic(val _unused_: Boolean = false) : TypeRef() {
            override fun toString() = stringRepresentation("TypeRef.Dynamic", listOf(
                ArgDesc("_unused_", _unused_.toString(), Companion.ArgType.VALUE)
            ))
        }
    }

    data class Type(
        override val mods: List<Modifier>,
        val ref: TypeRef
    ) : Node(), WithModifiers

    data class ValueArg(
        val name: String?,
        val asterisk: Boolean,
        val expr: Expr
    ) : Node()

    sealed class Expr : Node() {
        data class If(
            val expr: Expr,
            val body: Expr,
            val elseBody: Expr?
        ) : Expr()
        data class Try(
            val block: Block,
            val catches: List<Catch>,
            val finallyBlock: Block?
        ) : Expr() {
            data class Catch(
                override val anns: List<Modifier.AnnotationSet>,
                val varName: String,
                val varType: TypeRef.Simple,
                val block: Block
            ) : Node(), WithAnnotations
        }
        data class For(
            override val anns: List<Modifier.AnnotationSet>,
            // More than one means destructure, null means underscore
            val vars: List<Decl.Property.Var?>,
            val inExpr: Expr,
            val body: Expr
        ) : Expr(), WithAnnotations
        data class While(
            val expr: Expr,
            val body: Expr,
            val doWhile: Boolean
        ) : Expr()
        data class BinaryOp(
            val lhs: Expr,
            val oper: Oper,
            val rhs: Expr
        ) : Expr() {
            sealed class Oper : Node() {
                data class Infix(val str: String) : Oper()
                data class Token(val token: BinaryOp.Token) : Oper()
            }
            enum class Token(val str: String) {
                MUL("*"), DIV("/"), MOD("%"), ADD("+"), SUB("-"),
                IN("in"), NOT_IN("!in"),
                GT(">"), GTE(">="), LT("<"), LTE("<="),
                EQ("=="), NEQ("!="),
                ASSN("="), MUL_ASSN("*="), DIV_ASSN("/="), MOD_ASSN("%="), ADD_ASSN("+="), SUB_ASSN("-="),
                OR("||"), AND("&&"), ELVIS("?:"), RANGE("stdlib"),
                DOT("."), DOT_SAFE("?."), SAFE("?")
            }
        }
        data class UnaryOp(
            val expr: Expr,
            val oper: Oper,
            val prefix: Boolean
        ) : Expr() {
            data class Oper(val token: Token) : Node()
            enum class Token(val str: String) {
                NEG("-"), POS("+"), INC("++"), DEC("--"), NOT("!"), NULL_DEREF("!!")
            }
        }
        data class TypeOp(
            val lhs: Expr,
            val oper: Oper,
            val rhs: Type
        ) : Expr() {
            data class Oper(val token: Token) : Node()
            enum class Token(val str: String) {
                AS("as"), AS_SAFE("as?"), COL(":"), IS("is"), NOT_IS("!is")
            }
        }
        sealed class DoubleColonRef : Expr() {
            abstract val recv: Recv?
            data class Callable(
                override val recv: Recv?,
                val name: String
            ) : DoubleColonRef()
            data class Class(
                override val recv: Recv?
            ) : DoubleColonRef()
            sealed class Recv : Node() {
                data class Expr(val expr: Node.Expr) : Recv()
                data class Type(
                    val type: TypeRef.Simple,
                    val questionMarks: Int
                ) : Recv()
            }
        }
        data class Paren(
            val expr: Expr
        ) : Expr()
        data class StringTmpl(
            val elems: List<Elem>,
            val raw: Boolean
        ) : Expr() {
            sealed class Elem : Node() {
                data class Regular(val str: String) : Elem()
                data class ShortTmpl(val str: String) : Elem()
                data class UnicodeEsc(val digits: String) : Elem()
                data class RegularEsc(val char: Char) : Elem()
                data class LongTmpl(val expr: Expr) : Elem()
            }
        }
        data class Const(
            val value: String,
            val form: Form
        ) : Expr() {
            enum class Form { BOOLEAN, CHAR, INT, FLOAT, NULL }
        }
        data class Brace(
            val params: List<Param>,
            val block: Block?
        ) : Expr() {
            data class Param(
                // Multiple means destructure, null means underscore
                val vars: List<Decl.Property.Var?>,
                val destructType: Type?
            ) : Expr()
        }
        data class This(
            val label: String?
        ) : Expr()
        data class Super(
            val typeArg: Type?,
            val label: String?
        ) : Expr()
        data class When(
            val expr: Expr?,
            val entries: List<Entry>
        ) : Expr() {
            data class Entry(
                val conds: List<Cond>,
                val body: Expr
            ) : Node()
            sealed class Cond : Node() {
                data class Expr(val expr: Node.Expr) : Cond()
                data class In(
                    val expr: Node.Expr,
                    val not: Boolean
                ) : Cond()
                data class Is(
                    val type: Type,
                    val not: Boolean
                ) : Cond()
            }
        }
        data class Object(
            val parents: List<Decl.Structured.Parent>,
            val members: List<Decl>
        ) : Expr()
        data class Throw(
            val expr: Expr
        ) : Expr()
        data class Return(
            val label: String?,
            val expr: Expr?
        ) : Expr()
        data class Continue(
            val label: String?
        ) : Expr()
        data class Break(
            val label: String?
        ) : Expr()
        data class CollLit(
            val exprs: List<Expr>
        ) : Expr()
        data class Name(
            val name: String
        ) : Expr()
        data class Labeled(
            val label: String,
            val expr: Expr
        ) : Expr()
        data class Annotated(
            override val anns: List<Modifier.AnnotationSet>,
            val expr: Expr
        ) : Expr(), WithAnnotations
        data class Call(
            val expr: Expr,
            val typeArgs: List<Type?>,
            val args: List<ValueArg>,
            val lambda: TrailLambda?
        ) : Expr() {
            data class TrailLambda(
                override val anns: List<Modifier.AnnotationSet>,
                val label: String?,
                val func: Brace
            ) : Node(), WithAnnotations
        }
        data class ArrayAccess(
            val expr: Expr,
            val indices: List<Expr>
        ) : Expr()
        data class AnonFunc(
            val func: Decl.Func
        ) : Expr()
        // This is only present for when expressions and labeled expressions
        data class Property(
            val decl: Decl.Property
        ) : Expr()
    }

    data class Block(val stmts: List<Stmt>) : Node()
    sealed class Stmt : Node() {
        data class Decl(val decl: Node.Decl) : Stmt()
        data class Expr(val expr: Node.Expr) : Stmt()
    }

    sealed class Modifier : Node() {
        data class AnnotationSet(
            val target: Target?,
            val anns: List<Annotation>
        ) : Modifier() {
            enum class Target {
                FIELD, FILE, PROPERTY, GET, SET, RECEIVER, PARAM, SETPARAM, DELEGATE
            }
            data class Annotation(
                val names: List<String>,
                val typeArgs: List<Type>,
                val args: List<ValueArg>
            ) : Node()
        }
        data class Lit(val keyword: Keyword) : Modifier()
        enum class Keyword {
            ABSTRACT, FINAL, OPEN, ANNOTATION, SEALED, DATA, OVERRIDE, LATEINIT, INNER,
            PRIVATE, PROTECTED, PUBLIC, INTERNAL,
            IN, OUT, NOINLINE, CROSSINLINE, VARARG, REIFIED,
            TAILREC, OPERATOR, INFIX, INLINE, EXTERNAL, SUSPEND, CONST,
            ACTUAL, EXPECT
        }
    }

    sealed class Extra : Node() {
        data class BlankLines(
            val count: Int
        ) : Extra()
        data class Comment(
            val text: String,
            val startsLine: Boolean,
            val endsLine: Boolean
        ) : Extra()
    }
}