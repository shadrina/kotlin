class A {
    val x = 1
}

with<A> class B {
    val prop = <!UNRESOLVED_REFERENCE!>x<!> + <!UNRESOLVED_LABEL!>this@A<!>.<!UNRESOLVED_REFERENCE!>x<!>

    fun f() = <!UNRESOLVED_REFERENCE!>x<!> + <!UNRESOLVED_LABEL!>this@A<!>.<!UNRESOLVED_REFERENCE!>x<!>
}