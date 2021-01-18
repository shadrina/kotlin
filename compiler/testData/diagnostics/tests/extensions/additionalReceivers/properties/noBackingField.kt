interface A {
    fun a(): Int
}
interface B {
    fun b(): Int
}

with<A> val a = <!ADDITIONAL_RECEIVERS_WITH_BACKING_FIELD!>1<!>

with<A> with<B> var b = <!ADDITIONAL_RECEIVERS_WITH_BACKING_FIELD!>2<!>

with<A> with<B> val c get() = a() + b()