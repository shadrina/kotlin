class A<T>(val a: T)
class B(val b: Any)

with<A<Int>>
with<A<String>>
with<B>
fun f() {
    this@A.a.length
    this@B.b
    <!NO_THIS!>this<!>
}