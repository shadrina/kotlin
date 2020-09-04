// !DIAGNOSTICS: -UNUSED_PARAMETER

class Outer {
    val x: Int = 1
}

with<Outer>
class Inner(arg: Any) {
    fun bar() = x
}

fun f(outer: Outer) {
    Inner(1)
    outer.Inner(2)
    with(outer) {
        Inner(3)
    }
}