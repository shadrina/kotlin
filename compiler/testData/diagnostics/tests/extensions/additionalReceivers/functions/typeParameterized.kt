// !DIAGNOSTICS: -UNUSED_PARAMETER

class A
class B<X>(val x: X)

with<T> fun <T> T.f(t: B<T>) {}

fun Int.main(a: A, b: B<String>) {
    a.f(<!TYPE_MISMATCH, TYPE_MISMATCH!>b<!>)
}