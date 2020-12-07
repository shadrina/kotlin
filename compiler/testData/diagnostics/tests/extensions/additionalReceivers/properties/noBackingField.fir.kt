interface A {
    fun a(): Int
}
interface B {
    fun b(): Int
}

with<A> val a = 1

with<A> with<B> var b = 2

with<A> with<B> val c get() = <!UNRESOLVED_REFERENCE!>a<!>() + <!UNRESOLVED_REFERENCE!>b<!>()