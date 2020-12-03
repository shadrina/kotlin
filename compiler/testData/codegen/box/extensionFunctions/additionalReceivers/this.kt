class A<T>(val a: T)
class B(val b: Any?)

with<A<String>, B> fun f() {
    this@A.a.length
    this@B.b
}

fun box(): String {
    with(A("")) {
        with(B(null)) {
            f()
        }
    }
    return "OK"
}

