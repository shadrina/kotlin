class A(val a: String?)

with<A> fun f() {
    if (this@A.a == null) return
    <!DEBUG_INFO_SMARTCAST!>this@A.a<!>.length
}