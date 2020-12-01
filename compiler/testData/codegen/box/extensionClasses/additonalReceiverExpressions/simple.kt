class A {
    val a = 1
}

class B with(A()) {
    val b = a
}

fun box(): String = "OK"