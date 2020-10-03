class A {
    val o = "O"
}
class B {
    val k = "K"
}

with<A, B> fun f() = o + k

fun box() = with(A()) {
    with(B()) {
        f()
    }
}