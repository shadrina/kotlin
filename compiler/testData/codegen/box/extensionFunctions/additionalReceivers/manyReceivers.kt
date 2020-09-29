class A {
    val a = 1
}

class B {
    val b = 2
}

class C {
    val c = 3
}

with<A, B> fun C.f() {

}

fun box(): String {
    val a = A()
    val b = B()
    val c = C()
    with (a) {
        with(b) {
            with(c) {
                f()
            }
        }
    }
    return "OK"
}