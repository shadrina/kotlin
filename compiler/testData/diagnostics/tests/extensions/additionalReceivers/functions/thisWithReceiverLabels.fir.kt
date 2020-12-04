class A<T>(val a: T)
class B(val b: Any)
class C(val c: Any)

with<A<Int>> with<A<String>> with<B> fun f() {
    this@A.a.length
    this@B.b
    <!NO_THIS!>this<!>
}

with<A<Int>> with<A<String>> with<B> fun C.f() {
    this@A.a.length
    this@B.b
    this@C.c
    this@f.c
    this.c
}