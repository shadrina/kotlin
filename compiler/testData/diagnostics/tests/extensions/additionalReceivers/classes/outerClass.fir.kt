// !DIAGNOSTICS: -UNUSED_PARAMETER

class Outer {
    val x: Int = 1
}

with<Outer>
class Inner(arg: Any) {
    fun bar() = <!UNRESOLVED_REFERENCE!>x<!>
}

fun f(outer: Outer) {
    Inner(1)
    with(outer) {
        Inner(3)
    }
}