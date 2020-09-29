// !DIAGNOSTICS: -UNUSED_PARAMETER

class Outer {
    val x: Int = 1
}

with<Outer>
class Inner(arg: Any) {
    fun bar() = x
}

fun f(outer: Outer) {
    <!NO_ADDITIONAL_RECEIVER!>Inner(1)<!>
    with(outer) {
        Inner(3)
    }
}