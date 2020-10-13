class A(val a: Any?)
class B(val b: Any?)

with<A, B>
class C {
    fun f() {
        <!UNRESOLVED_REFERENCE!>a<!>
        <!UNRESOLVED_REFERENCE!>b<!>
    }
}