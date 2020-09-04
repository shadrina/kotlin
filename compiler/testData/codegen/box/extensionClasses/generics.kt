with<T> class B<T : CharSequence> {
    val result = if (length == 2) "OK" else "fail"
}

fun box() = "OK".B().result