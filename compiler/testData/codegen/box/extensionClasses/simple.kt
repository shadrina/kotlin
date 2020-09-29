class A {
    val ok = "OK"
}

with<A>
class B {
    fun result() = ok
}

fun box(): String {
    with(A()) {
        return B().result()
    }
}