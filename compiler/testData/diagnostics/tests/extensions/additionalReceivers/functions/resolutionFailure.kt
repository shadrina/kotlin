class A<T>

with<A<Int>> fun g() {}

fun some() {
    with(A<Int>()) {
        with(A<String>()) {
            <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>g<!>()
        }
    }
}