// WITH_RUNTIME

with<Collection<P>> class A<P> {
    val result = if (!isEmpty()) "OK" else "fail"
}

fun box(): String {
    with (listOf(1, 2, 3)) {
        return A().result
    }
}