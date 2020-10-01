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
    this@A.a
    this@f.b
    this.b
}

with<A, B> fun g() {
    this@A.a
    this@B.b
    this.b
}

with<C<Int>, C<String>> fun h() {
    this@`C<Int>`.c
    this@`C<String>`.c
}

class D(val aField: A) {
    with<A> fun B.f() {
        this.b
        this@f.b
        this@A.a
        this@D.aField
        return<!UNRESOLVED_REFERENCE!>@A<!>
    }

    with<A, B> fun g() {
        this@A.a
        this@B.b
        this@D.aField
    }
}