// !DIAGNOSTICS: -UNUSED_PARAMETER

object Some1 {
    val some1 = 1
}
object Some2 {
    val some2 = 2
}

fun Some1.some1() {}
with<Some2> fun some2() {}

class A with(Some1) with(Some2) {
    init {
        <!UNRESOLVED_REFERENCE!>some1<!>
        <!UNRESOLVED_REFERENCE!>some2<!>
        <!INAPPLICABLE_CANDIDATE!>some1<!>()
        some2()
    }

    constructor() {
        <!UNRESOLVED_REFERENCE!>some1<!>
        <!UNRESOLVED_REFERENCE!>some2<!>
        <!INAPPLICABLE_CANDIDATE!>some1<!>()
        some2()
    }

    val a1 = <!UNRESOLVED_REFERENCE!>some1<!>
    fun a2(param: Any?) = <!UNRESOLVED_REFERENCE!>some2<!>
    fun a3(param: Any?) {
        <!UNRESOLVED_REFERENCE!>some1<!>
        <!UNRESOLVED_REFERENCE!>some2<!>
    }

    inner class B {
        val b = <!UNRESOLVED_REFERENCE!>some1<!> + <!UNRESOLVED_REFERENCE!>some2<!>
    }
}

with<A> fun f() {
    <!UNRESOLVED_REFERENCE!>a1<!>
    <!UNRESOLVED_REFERENCE!>a2<!>(null)
    <!UNRESOLVED_REFERENCE!>a3<!>(null)
}