class A<T>(val a: T)
class B(val b: Any)

with<A<Int>, A<String>, B> fun f() {
    <!UNRESOLVED_LABEL!>this@A<!>.<!UNRESOLVED_REFERENCE!>a<!>.<!UNRESOLVED_REFERENCE!>length<!>
    <!UNRESOLVED_LABEL!>this@B<!>.<!UNRESOLVED_REFERENCE!>b<!>
    <!NO_THIS!>this<!>
}