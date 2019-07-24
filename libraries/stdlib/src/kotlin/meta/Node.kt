/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package kotlin.meta

sealed class Node {

    /**
     * Author didn't forget about the existence of reflection.
     * The use of reflection in stdlib is prohibited, so the author resorts
     * to such terrible methods. =(
     */

    var tag: Any? = null

    companion object {
        const val PREFIX = "kotlin.meta.Node."
    }

    abstract fun toCode(): String
    
    protected fun List<Node?>.toCode() = "listOf(${this.joinToString(", ") { it?.toCode() ?: it.toString() }})"
    
    protected fun stringify(s: String?)       = if (s == null) s.toString() else "\"" + s + "\""
    protected fun stringify(l: List<String?>) = l.map { "\"" + l.toString() + "\"" }.toString()
    protected fun stringRepresentation(className: String, vararg args: Pair<String, String?>) =
        "$PREFIX$className(${args.joinToString(", ") { "${it.first}=${it.second}" }})"

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
        override fun toCode() = stringRepresentation(
            "File",
            "anns" to anns.toCode(),
            "pkg" to pkg?.toCode(),
            "imports" to imports.toCode(),
            "decls" to decls.toCode()
        )
    }

    data class Script(
        override val anns: List<Modifier.AnnotationSet>,
        override val pkg: Package?,
        override val imports: List<Import>,
        val exprs: List<Expr>
    ) : Node(), Entry {
        override fun toCode() = stringRepresentation(
            "Script",
            "anns" to anns.toCode(),
            "pkg" to pkg?.toCode(),
            "imports" to imports.toCode(),
            "exprs" to exprs.toCode()
        )
    }

    data class Package(
        override val mods: List<Modifier>,
        val names: List<String>
    ) : Node(), WithModifiers {
        override fun toCode() = stringRepresentation(
            "Package",
            "mods" to mods.toCode(),
            "names" to stringify(names)
        )
    }

    data class Import(
        val names: List<String>,
        val wildcard: Boolean,
        val alias: String?
    ) : Node() {
        override fun toCode() = stringRepresentation(
            "Import",
            "names" to stringify(names),
            "wildcard" to wildcard.toString(),
            "alias" to stringify(alias)
        )
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
            override fun toCode() = stringRepresentation(
                "Decl.Structured",
                "mods" to mods.toCode(),
                "form" to form.toCode(),
                "name" to stringify(name),
                "typeParams" to typeParams.toCode(),
                "primaryConstructor" to primaryConstructor?.toCode(),
                "parentAnns" to parentAnns.toCode(),
                "parents" to parents.toCode(),
                "typeConstraints" to typeConstraints.toCode(),
                "members" to members.toCode()
            )

            enum class Form {
                CLASS, ENUM_CLASS, INTERFACE, OBJECT, COMPANION_OBJECT;

                fun toCode() = PREFIX + "Decl.Structured.Form." + super.toString()
            }

            sealed class Parent : Node() {
                data class CallConstructor(
                    val type: TypeRef.Simple,
                    val typeArgs: List<Node.Type?>,
                    val args: List<ValueArg>,
                    val lambda: Expr.Call.TrailLambda?
                ) : Parent() {
                    override fun toCode() = stringRepresentation(
                        "Decl.Structured.Parent.CallConstructor",
                        "type" to type.toCode(),
                        "typeArgs" to typeArgs.toCode(),
                        "args" to args.toCode(),
                        "lambda" to lambda?.toCode()
                    )
                }

                data class Type(
                    val type: TypeRef.Simple,
                    val by: Expr?
                ) : Parent() {
                    override fun toCode() = stringRepresentation(
                        "Decl.Structured.Parent.Type",
                        "type" to type.toCode(),
                        "by" to by?.toCode()
                    )
                }
            }

            data class PrimaryConstructor(
                override val mods: List<Modifier>,
                val params: List<Func.Param>
            ) : Node(), WithModifiers {
                override fun toCode() = stringRepresentation(
                    "Decl.Structured.PrimaryConstructor",
                    "mods" to mods.toCode(),
                    "params" to params.toCode()
                )
            }
        }

        data class Init(val block: Block) : Decl() {
            override fun toCode() = stringRepresentation(
                "Decl.Init",
                "block" to block.toCode()
            )
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
            override fun toCode() = stringRepresentation(
                "Decl.Func",
                "mods" to mods.toCode(),
                "typeParams" to typeParams.toCode(),
                "receiverType" to receiverType?.toCode(),
                "name" to stringify(name),
                "paramTypeParams" to paramTypeParams.toCode(),
                "params" to params.toCode(),
                "type" to type?.toCode(),
                "typeConstraints" to typeConstraints.toCode(),
                "body" to body?.toCode()
            )

            data class Param(
                override val mods: List<Modifier>,
                val readOnly: Boolean?,
                val name: String,
                // Type can be null for anon functions
                val type: Type?,
                val default: Expr?
            ) : Node(), WithModifiers {
                override fun toCode() = stringRepresentation(
                    "Decl.Func.Param",
                    "mods" to mods.toCode(),
                    "readOnly" to readOnly.toString(),
                    "name" to stringify(name),
                    "type" to type?.toCode(),
                    "default" to default?.toCode()
                )
            }

            sealed class Body : Node() {
                data class Block(val block: Node.Block) : Body() {
                    override fun toCode() = stringRepresentation(
                        "Decl.Func.Body.Block",
                        "block" to block.toCode()
                    )
                }

                data class Expr(val expr: Node.Expr) : Body() {
                    override fun toCode() = stringRepresentation(
                        "Decl.Func.Body.Expr",
                        "expr" to expr.toCode()
                    )
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
            override fun toCode() = stringRepresentation(
                "Decl.Property",
                "mods" to mods.toCode(),
                "readOnly" to readOnly.toString(),
                "typeParams" to typeParams.toCode(),
                "receiverType" to receiverType?.toCode(),
                "vars" to vars.toCode(),
                "typeConstraints" to typeConstraints.toCode(),
                "delegated" to delegated.toString(),
                "expr" to expr?.toCode(),
                "accessors" to accessors?.toCode()
            )

            data class Var(
                val name: String,
                val type: Type?
            ) : Node() {
                override fun toCode() = stringRepresentation(
                    "Decl.Property.Var",
                    "name" to stringify(name),
                    "type" to type?.toCode()
                )
            }

            data class Accessors(
                val first: Accessor,
                val second: Accessor?
            ) : Node() {
                override fun toCode() = stringRepresentation(
                    "Decl.Property.Accessors",
                    "first" to first.toCode(),
                    "second" to second?.toCode()
                )
            }

            sealed class Accessor : Node(), WithModifiers {
                data class Get(
                    override val mods: List<Modifier>,
                    val type: Type?,
                    val body: Func.Body?
                ) : Accessor() {
                    override fun toCode() = stringRepresentation(
                        "Decl.Property.Accessor.Get",
                        "mods" to mods.toCode(),
                        "type" to type?.toCode(),
                        "body" to body?.toCode()
                    )
                }

                data class Set(
                    override val mods: List<Modifier>,
                    val paramMods: List<Modifier>,
                    val paramName: String?,
                    val paramType: Type?,
                    val body: Func.Body?
                ) : Accessor() {
                    override fun toCode() = stringRepresentation(
                        "Decl.Property.Accessor.Set",
                        "mods" to mods.toCode(),
                        "paramMods" to paramMods.toCode(),
                        "paramName" to stringify(paramName),
                        "paramType" to paramType?.toCode(),
                        "body" to body?.toCode()
                    )
                }
            }
        }

        data class TypeAlias(
            override val mods: List<Modifier>,
            val name: String,
            val typeParams: List<TypeParam>,
            val type: Type
        ) : Decl(), WithModifiers {
            override fun toCode() = stringRepresentation(
                "Decl.TypeAlias",
                "mods" to mods.toCode(),
                "name" to stringify(name),
                "typeParams" to typeParams.toCode(),
                "type" to type.toCode()
            )
        }

        data class Constructor(
            override val mods: List<Modifier>,
            val params: List<Func.Param>,
            val delegationCall: DelegationCall?,
            val block: Block?
        ) : Decl(), WithModifiers {
            override fun toCode() = stringRepresentation(
                "Decl.Constructor",
                "mods" to mods.toCode(),
                "params" to params.toCode(),
                "delegationCall" to delegationCall?.toCode(),
                "block" to block?.toCode()
            )

            data class DelegationCall(
                val target: DelegationTarget,
                val args: List<ValueArg>
            ) : Node() {
                override fun toCode() = stringRepresentation(
                    "Decl.Constructor.DelegationCall",
                    "target" to target.toCode(),
                    "args" to args.toCode()
                )
            }

            enum class DelegationTarget {
                THIS, SUPER;

                fun toCode() = PREFIX + "Decl.Constructor.DelegationTarget." + super.toString()
            }
        }

        data class EnumEntry(
            override val mods: List<Modifier>,
            val name: String,
            val args: List<ValueArg>,
            val members: List<Decl>
        ) : Decl(), WithModifiers {
            override fun toCode() = stringRepresentation(
                "Decl.EnumEntry",
                "mods" to mods.toCode(),
                "name" to stringify(name),
                "args" to args.toCode(),
                "members" to members.toCode()
            )
        }
    }

    data class TypeParam(
        override val mods: List<Modifier>,
        val name: String,
        val type: TypeRef?
    ) : Node(), WithModifiers {
        override fun toCode() = stringRepresentation(
            "TypeParam",
            "mods" to mods.toCode(),
            "name" to stringify(name),
            "type" to type?.toCode()
        )
    }

    data class TypeConstraint(
        override val anns: List<Modifier.AnnotationSet>,
        val name: String,
        val type: Type
    ) : Node(), WithAnnotations {
        override fun toCode() = stringRepresentation(
            "TypeConstraint",
            "anns" to anns.toCode(),
            "name" to stringify(name),
            "type" to type.toCode()
        )
    }

    sealed class TypeRef : Node() {
        data class Paren(
            override val mods: List<Modifier>,
            val type: TypeRef
        ) : TypeRef(), WithModifiers {
            override fun toCode() = stringRepresentation(
                "TypeRef.Paren",
                "mods" to mods.toCode(),
                "type" to type.toCode()
            )
        }

        data class Func(
            val receiverType: Type?,
            val params: List<Param>,
            val type: Type
        ) : TypeRef() {
            override fun toCode() = stringRepresentation(
                "TypeRef.Func",
                "receiverType" to receiverType?.toCode(),
                "params" to params.toCode(),
                "type" to type.toCode()
            )

            data class Param(
                val name: String?,
                val type: Type
            ) : Node() {
                override fun toCode() = stringRepresentation(
                    "TypeRef.Func.Param",
                    "name" to stringify(name),
                    "type" to type.toCode()
                )
            }
        }

        data class Simple(
            val pieces: List<Piece>
        ) : TypeRef() {
            override fun toCode() = stringRepresentation(
                "TypeRef.Simple",
                "pieces" to pieces.toCode()
            )

            data class Piece(
                val name: String,
                // Null means any
                val typeParams: List<Type?>
            ) : Node() {
                override fun toCode() = stringRepresentation(
                    "TypeRef.Simple.Piece",
                    "name" to stringify(name),
                    "typeParams" to typeParams.toCode()
                )
            }
        }

        data class Nullable(val type: TypeRef) : TypeRef() {
            override fun toCode() = stringRepresentation(
                "TypeRef.Nullable",
                "type" to type.toCode()
            )
        }

        data class Dynamic(val _unused_: Boolean = false) : TypeRef() {
            override fun toCode() = stringRepresentation(
                "TypeRef.Dynamic",
                "_unused_" to _unused_.toString()
            )
        }
    }

    data class Type(
        override val mods: List<Modifier>,
        val ref: TypeRef
    ) : Node(), WithModifiers {
        override fun toCode() = stringRepresentation(
            "Type",
            "mods" to mods.toCode(),
            "ref" to ref.toCode()
        )
    }

    data class ValueArg(
        val name: String?,
        val asterisk: Boolean,
        val expr: Expr
    ) : Node() {
        override fun toCode() = stringRepresentation(
            "ValueArg",
            "name" to stringify(name),
            "asterisk" to asterisk.toString(),
            "expr" to expr.toCode()
        )
    }

    sealed class Expr : Node() {
        data class If(
            val expr: Expr,
            val body: Expr,
            val elseBody: Expr?
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.If",
                "expr" to expr.toCode(),
                "body" to body.toCode(),
                "elseBody" to elseBody?.toCode()
            )
        }

        data class Try(
            val block: Block,
            val catches: List<Catch>,
            val finallyBlock: Block?
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.Try",
                "block" to block.toCode(),
                "catches" to catches.toCode(),
                "elseBody" to finallyBlock?.toCode()
            )

            data class Catch(
                override val anns: List<Modifier.AnnotationSet>,
                val varName: String,
                val varType: TypeRef.Simple,
                val block: Block
            ) : Node(), WithAnnotations {
                override fun toCode() = stringRepresentation(
                    "Expr.Try.Catch",
                    "anns" to anns.toCode(),
                    "varName" to stringify(varName),
                    "varType" to varType.toCode(),
                    "block" to block.toCode()
                )
            }
        }

        data class For(
            override val anns: List<Modifier.AnnotationSet>,
            // More than one means destructure, null means underscore
            val vars: List<Decl.Property.Var?>,
            val inExpr: Expr,
            val body: Expr
        ) : Expr(), WithAnnotations {
            override fun toCode() = stringRepresentation(
                "Expr.For",
                "anns" to anns.toCode(),
                "vars" to vars.toCode(),
                "inExpr" to inExpr.toCode(),
                "body" to body.toCode()
            )
        }

        data class While(
            val expr: Expr,
            val body: Expr,
            val doWhile: Boolean
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.While",
                "expr" to expr.toCode(),
                "body" to body.toCode(),
                "doWhile" to doWhile.toString()
            )
        }

        data class BinaryOp(
            val lhs: Expr,
            val oper: Oper,
            val rhs: Expr
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.BinaryOp",
                "lhs" to lhs.toCode(),
                "oper" to oper.toCode(),
                "rhs" to rhs.toCode()
            )

            sealed class Oper : Node() {
                data class Infix(val str: String) : Oper() {
                    override fun toCode() = stringRepresentation(
                        "Expr.BinaryOp.Oper.Infix",
                        "str" to stringify(str)
                    )
                }

                data class Token(val token: BinaryOp.Token) : Oper() {
                    override fun toCode() = stringRepresentation(
                        "Expr.BinaryOp.Oper.Token",
                        "token" to token.toCode()
                    )
                }
            }

            enum class Token(val str: String) {
                MUL("*"), DIV("/"), MOD("%"), ADD("+"), SUB("-"),
                IN("in"), NOT_IN("!in"),
                GT(">"), GTE(">="), LT("<"), LTE("<="),
                EQ("=="), NEQ("!="),
                ASSN("="), MUL_ASSN("*="), DIV_ASSN("/="), MOD_ASSN("%="), ADD_ASSN("+="), SUB_ASSN("-="),
                OR("||"), AND("&&"), ELVIS("?:"), RANGE("stdlib"),
                DOT("."), DOT_SAFE("?."), SAFE("?");

                fun toCode() = PREFIX + "Expr.BinaryOp.Token." + super.toString()
            }
        }

        data class UnaryOp(
            val expr: Expr,
            val oper: Oper,
            val prefix: Boolean
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.UnaryOp",
                "expr" to expr.toCode(),
                "oper" to oper.toCode(),
                "prefix" to prefix.toString()
            )

            data class Oper(val token: Token) : Node() {
                override fun toCode() = stringRepresentation(
                    "Expr.UnaryOp.Oper",
                    "token" to token.toCode()
                )
            }

            enum class Token(val str: String) {
                NEG("-"), POS("+"), INC("++"), DEC("--"), NOT("!"), NULL_DEREF("!!");

                fun toCode() = PREFIX + "Expr.UnaryOp.Token." + super.toString()
            }
        }

        data class TypeOp(
            val lhs: Expr,
            val oper: Oper,
            val rhs: Type
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.TypeOp",
                "lhs" to lhs.toCode(),
                "oper" to oper.toCode(),
                "rhs" to rhs.toCode()
            )

            data class Oper(val token: Token) : Node() {
                override fun toCode() = stringRepresentation(
                    "Expr.TypeOp.Oper",
                    "token" to token.toCode()
                )
            }

            enum class Token(val str: String) {
                AS("as"), AS_SAFE("as?"), COL(":"), IS("is"), NOT_IS("!is");

                fun toCode() = PREFIX + "Expr.TypeOp.Token." + super.toString()
            }
        }

        sealed class DoubleColonRef : Expr() {
            abstract val recv: Recv?

            data class Callable(
                override val recv: Recv?,
                val name: String
            ) : DoubleColonRef() {
                override fun toCode() = stringRepresentation(
                    "Expr.DoubleColonRef.Callable",
                    "recv" to recv?.toCode(),
                    "name" to stringify(name)
                )
            }

            data class Class(
                override val recv: Recv?
            ) : DoubleColonRef() {
                override fun toCode() = stringRepresentation(
                    "Expr.DoubleColonRef.Class",
                    "recv" to recv?.toCode()
                )
            }

            sealed class Recv : Node() {
                data class Expr(val expr: Node.Expr) : Recv() {
                    override fun toCode() = stringRepresentation(
                        "Expr.DoubleColonRef.Recv.Expr",
                        "expr" to expr.toCode()
                    )
                }

                data class Type(
                    val type: TypeRef.Simple,
                    val questionMarks: Int
                ) : Recv() {
                    override fun toCode() = stringRepresentation(
                        "Expr.DoubleColonRef.Recv.Type",
                        "type" to type.toCode(),
                        "questionMarks" to questionMarks.toString()
                    )
                }
            }
        }

        data class Paren(
            val expr: Expr
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.Paren",
                "expr" to expr.toCode()
            )
        }

        data class StringTmpl(
            val elems: List<Elem>,
            val raw: Boolean
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.StringTmpl",
                "elems" to elems.toCode(),
                "raw" to raw.toString()
            )

            sealed class Elem : Node() {
                data class Regular(val str: String) : Elem() {
                    override fun toCode() = stringRepresentation(
                        "Expr.StringTmpl.Elem.Regular",
                        "str" to stringify(str)
                    )
                }

                data class ShortTmpl(val str: String) : Elem() {
                    override fun toCode() = stringRepresentation(
                        "Expr.StringTmpl.Elem.ShortTmpl",
                        "str" to stringify(str)
                    )
                }

                data class UnicodeEsc(val digits: String) : Elem() {
                    override fun toCode() = stringRepresentation(
                        "Expr.StringTmpl.Elem.UnicodeEsc",
                        "digits" to stringify(digits)
                    )
                }

                data class RegularEsc(val char: Char) : Elem() {
                    override fun toCode() = stringRepresentation(
                        "Expr.StringTmpl.Elem.RegularEsc",
                        "char" to char.toString()
                    )
                }

                data class LongTmpl(val expr: Expr) : Elem() {
                    override fun toCode() = stringRepresentation(
                        "Expr.StringTmpl.Elem.LongTmpl",
                        "expr" to expr.toCode()
                    )
                }
            }
        }

        data class Const(
            val value: String,
            val form: Form
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.Const",
                "value" to stringify(value),
                "form" to form.toCode()
            )

            enum class Form {
                BOOLEAN, CHAR, INT, FLOAT, NULL;

                fun toCode() = PREFIX + "Expr.Const.Form." + super.toString()
            }
        }

        data class Brace(
            val params: List<Param>,
            val block: Block?
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.Brace",
                "params" to params.toCode(),
                "block" to block?.toCode()
            )

            data class Param(
                // Multiple means destructure, null means underscore
                val vars: List<Decl.Property.Var?>,
                val destructType: Type?
            ) : Expr() {
                override fun toCode() = stringRepresentation(
                    "Expr.Brace.Param",
                    "vars" to vars.toCode(),
                    "destructType" to destructType?.toCode()
                )
            }
        }

        data class This(
            val label: String?
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.This",
                "label" to stringify(label)
            )
        }

        data class Super(
            val typeArg: Type?,
            val label: String?
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.Super",
                "typeArg" to typeArg?.toCode(),
                "label" to stringify(label)
            )
        }

        data class When(
            val expr: Expr?,
            val entries: List<Entry>
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.When",
                "expr" to expr?.toCode(),
                "entries" to entries.toCode()
            )

            data class Entry(
                val conds: List<Cond>,
                val body: Expr
            ) : Node() {
                override fun toCode() = stringRepresentation(
                    "Expr.When.Entry",
                    "conds" to conds.toCode(),
                    "body" to body.toCode()
                )
            }

            sealed class Cond : Node() {
                data class Expr(val expr: Node.Expr) : Cond() {
                    override fun toCode() = stringRepresentation(
                        "Expr.When.Cond.Expr",
                        "expr" to expr.toCode()
                    )
                }

                data class In(
                    val expr: Node.Expr,
                    val not: Boolean
                ) : Cond() {
                    override fun toCode() = stringRepresentation(
                        "Expr.When.Cond.In",
                        "expr" to expr.toCode(),
                        "not" to not.toString()
                    )
                }

                data class Is(
                    val type: Type,
                    val not: Boolean
                ) : Cond() {
                    override fun toCode() = stringRepresentation(
                        "Expr.When.Cond.Is",
                        "type" to type.toCode(),
                        "not" to not.toString()
                    )
                }
            }
        }

        data class Object(
            val parents: List<Decl.Structured.Parent>,
            val members: List<Decl>
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.Object",
                "parents" to parents.toCode(),
                "members" to members.toCode()
            )
        }

        data class Throw(
            val expr: Expr
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.Throw",
                "expr" to expr.toCode()
            )
        }

        data class Return(
            val label: String?,
            val expr: Expr?
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.Return",
                "label" to stringify(label),
                "expr" to expr?.toCode()
            )
        }

        data class Continue(
            val label: String?
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.Continue",
                "label" to stringify(label)
            )
        }

        data class Break(
            val label: String?
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.Break",
                "label" to stringify(label)
            )
        }

        data class CollLit(
            val exprs: List<Expr>
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.CollLit",
                "exprs" to exprs.toCode()
            )
        }

        data class Name(
            val name: String
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.Name",
                "name" to stringify(name)
            )
        }

        data class ExternalName(
            val name: String
        ) : Expr() {
            override fun toCode() = name
        }

        data class Labeled(
            val label: String,
            val expr: Expr
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.Labeled",
                "label" to stringify(label),
                "expr" to expr.toCode()
            )
        }

        data class Annotated(
            override val anns: List<Modifier.AnnotationSet>,
            val expr: Expr
        ) : Expr(), WithAnnotations {
            override fun toCode() = stringRepresentation(
                "Expr.Annotated",
                "anns" to anns.toCode(),
                "expr" to expr.toCode()
            )
        }

        data class Call(
            val expr: Expr,
            val typeArgs: List<Type?>,
            val args: List<ValueArg>,
            val lambda: TrailLambda?
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.Call",
                "expr" to expr.toCode(),
                "typeArgs" to typeArgs.toCode(),
                "args" to args.toCode(),
                "lambda" to lambda?.toCode()
            )

            data class TrailLambda(
                override val anns: List<Modifier.AnnotationSet>,
                val label: String?,
                val func: Brace
            ) : Node(), WithAnnotations {
                override fun toCode() = stringRepresentation(
                    "Expr.Call.TrailLambda",
                    "anns" to anns.toCode(),
                    "label" to stringify(label),
                    "func" to func.toCode()
                )
            }
        }

        data class ArrayAccess(
            val expr: Expr,
            val indices: List<Expr>
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.ArrayAccess",
                "expr" to expr.toCode(),
                "indices" to indices.toCode()
            )
        }

        data class AnonFunc(
            val func: Decl.Func
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.AnonFunc",
                "func" to func.toCode()
            )
        }

        // This is only present for when expressions and labeled expressions
        data class Property(
            val decl: Decl.Property
        ) : Expr() {
            override fun toCode() = stringRepresentation(
                "Expr.Property",
                "decl" to decl.toCode()
            )
        }
    }

    data class Block(val stmts: List<Stmt>) : Node() {
        override fun toCode() = stringRepresentation(
            "Block",
            "stmts" to stmts.toCode()
        )
    }

    sealed class Stmt : Node() {
        data class Decl(val decl: Node.Decl) : Stmt() {
            override fun toCode() = stringRepresentation(
                "Stmt.Decl",
                "decl" to decl.toCode()
            )
        }

        data class Expr(val expr: Node.Expr) : Stmt() {
            override fun toCode() = stringRepresentation(
                "Stmt.Expr",
                "expr" to expr.toCode()
            )
        }
    }

    sealed class Modifier : Node() {
        data class AnnotationSet(
            val target: Target?,
            val anns: List<Annotation>
        ) : Modifier() {
            override fun toCode() = stringRepresentation(
                "Modifier.AnnotationSet",
                "target" to target?.toCode(),
                "anns" to anns.toCode()
            )

            enum class Target {
                FIELD, FILE, PROPERTY, GET, SET, RECEIVER, PARAM, SETPARAM, DELEGATE, MACRO;

                fun toCode() = PREFIX + "Modifier.AnnotationSet.Target." + super.toString()
            }

            data class Annotation(
                val names: List<String>,
                val typeArgs: List<Type>,
                val args: List<ValueArg>
            ) : Node() {
                override fun toCode() = stringRepresentation(
                    "Modifier.AnnotationSet.Annotation",
                    "names" to stringify(names),
                    "typeArgs" to typeArgs.toCode(),
                    "args" to args.toCode()
                )
            }
        }

        data class Lit(val keyword: Keyword) : Modifier() {
            override fun toCode() = stringRepresentation(
                "Modifier.Lit",
                "keyword" to keyword.toCode()
            )
        }

        enum class Keyword {
            ABSTRACT, FINAL, OPEN, ANNOTATION, SEALED, DATA, OVERRIDE, LATEINIT, INNER,
            PRIVATE, PROTECTED, PUBLIC, INTERNAL,
            IN, OUT, NOINLINE, CROSSINLINE, VARARG, REIFIED,
            TAILREC, OPERATOR, INFIX, INLINE, EXTERNAL, SUSPEND, CONST,
            ACTUAL, EXPECT;

            fun toCode() = PREFIX + "Modifier.Keyword" + super.toString()
        }
    }

    sealed class Extra : Node() {
        data class BlankLines(
            val count: Int
        ) : Extra() {
            override fun toCode() = stringRepresentation(
                "Extra.BlankLines",
                "count" to count.toString()
            )
        }

        data class Comment(
            val text: String,
            val startsLine: Boolean,
            val endsLine: Boolean
        ) : Extra() {
            override fun toCode() = stringRepresentation(
                "Extra.Comment",
                "text" to stringify(text),
                "startsLine" to startsLine.toString(),
                "endsLine" to endsLine.toString()
            )
        }
    }
}