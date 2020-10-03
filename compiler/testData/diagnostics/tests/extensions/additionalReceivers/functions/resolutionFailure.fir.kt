class A<T>

with<A<Int>> fun g() {}

fun some() {
    with(A<Int>()) {
        with(A<String>()) {
            g()
        }
    }
}