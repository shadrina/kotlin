// !DIAGNOSTICS: -UNUSED_PARAMETER

object Some1 {
    val some1 = 1
}
object Some2 {
    val some2 = 2
}

fun Some1.some1() {}
with<Some2> fun some2() {}

class A with(Some1) with(Some2) {
    init {
        some1
        some2
        some1()
        some2()
    }

    constructor() {
        some1
        some2
        some1()
        some2()
    }

    val a1 = some1
    fun a2(param: Any?) = some2
    fun a3(param: Any?) {
        some1
        some2
    }

    inner class B {
        val b = some1 + some2
    }
}

with<A> fun f() {
    a1
    a2(null)
    a3(null)
}