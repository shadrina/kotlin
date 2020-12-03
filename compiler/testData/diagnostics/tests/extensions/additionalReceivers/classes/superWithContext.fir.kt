interface Context {
    fun h() {}
}

open class A {
    open fun f() {}
}

class B : A() {
    override fun f() {}

    with<Context>
    inner class C {
        fun g() {
            super@B.f()
            super@Context.<!UNRESOLVED_REFERENCE!>h<!>()
        }
    }
}