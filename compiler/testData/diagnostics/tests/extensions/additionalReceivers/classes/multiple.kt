class A(val a: Any?)
class B(val b: Any?)

with<A>
with<B>
class C {
    fun f() {
        a
        b
    }
}