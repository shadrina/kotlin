object Some

class A with(<!DEBUG_INFO_MISSING_UNRESOLVED!>Some<!>)

with<A> fun f() {}