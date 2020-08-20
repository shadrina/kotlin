class A
class B
class C

with<A> fun B.f() {}
with<A> fun B.g() {
    f()
}
with<A> fun C.h() {
    <!INAPPLICABLE_CANDIDATE!>f<!>()
}

fun A.q(b: B) {
    with(b) {
        f()
    }
    <!INAPPLICABLE_CANDIDATE!>f<!>()
}