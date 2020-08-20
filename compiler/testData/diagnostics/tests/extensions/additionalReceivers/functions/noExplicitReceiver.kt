class A
class B
class C

with<A> fun B.f() {}
with<A> fun B.g() {
    f()
}
with<A> fun C.h() {
    <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>f<!>()
}

fun A.q(b: B) {
    with(b) {
        f()
    }
    <!UNRESOLVED_REFERENCE!>f<!>()
}