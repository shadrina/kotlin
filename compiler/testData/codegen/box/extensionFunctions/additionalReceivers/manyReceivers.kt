class A {
    val o = "O"
}
class B {
    val k = "K"
}

with<A> with<B> fun f() = o + k

fun box() = with(A()) {
    with(B()) {
        f()
    }
}