class A {
    val a = 1
}

class B {
    val b = 2
}

class C<T> {
    val c = 3
}

with<A> fun B.f() {
    this@A.<!UNRESOLVED_REFERENCE!>a<!>
    this@f.b
    this.b
}

with<A, B> fun g() {
    this@A.<!UNRESOLVED_REFERENCE!>a<!>
    this@B.<!UNRESOLVED_REFERENCE!>b<!>
    this.<!UNRESOLVED_REFERENCE!>b<!>
}

with<C<Int>, C<String>> fun h() {
    this@`C<Int>`.<!UNRESOLVED_REFERENCE!>c<!>
    this@`C<String>`.<!UNRESOLVED_REFERENCE!>c<!>
}

class D(val aField: A) {
    with<A> fun B.f() {
        this.b
        this@f.b
        this@A.<!UNRESOLVED_REFERENCE!>a<!>
        this@D.aField
        return@A
    }

    with<A, B> fun g() {
        this@A.<!UNRESOLVED_REFERENCE!>a<!>
        this@B.<!UNRESOLVED_REFERENCE!>b<!>
        this@D.aField
    }
}