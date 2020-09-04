class A {
    val ok = "OK"
}

with<A>
class B {
    fun result() = ok
}

fun box(): String {
    return A().B().result()
}