// !DIAGNOSTICS: -UNUSED_PARAMETER

class A<T>(val a: T)
open class B(val b: Any)
class C(val c: Any) : B(c)

fun f(a: A<String>, b: B, c: C) with(a) with(b) with(c) {
    <!NO_THIS!>this<A<Int>><!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>a<!>
    this<A<String>>.a
    this<B>.b
    this<C>.c
}

with<A<String>> with<C> fun g() {
    <!NO_THIS!>this<A<Int>><!>.<!DEBUG_INFO_MISSING_UNRESOLVED!>a<!>
    this<A<String>>.a
    this<B>.b
    this<B>.c
}