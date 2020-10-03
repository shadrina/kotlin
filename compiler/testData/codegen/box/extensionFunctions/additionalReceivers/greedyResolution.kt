open class A {
    open val a = "fail"
}

class B : A() {
    override val a = "OK"
}

with<A> fun f(action: A.() -> String) = action()

fun box() = with(A()) {
    with(B()) {
        f { a }
    }
}
