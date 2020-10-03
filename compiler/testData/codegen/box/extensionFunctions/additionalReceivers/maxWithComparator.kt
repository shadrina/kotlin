// WITH_RUNTIME

with<Comparator<T>> fun <T> Iterable<T>.maxOrNull(): T? {
    val iterator = iterator()
    if (!iterator.hasNext()) {
        return null
    }
    var max = iterator.next()
    while (iterator.hasNext()) {
        val e = iterator.next()
        if (compare(e, max) > 0) {
            max = e
        }
    }
    return max
}

fun box(): String {
    val strings = listOf("O", "OK", "K")
    val comparator = Comparator<String> { o1, o2 ->
        val length1 = o1.length
        val length2 = o2.length
        when {
            length1 < length2 -> -1
            length1 == length2 -> 0
            else -> 1
        }
    }
    return with(comparator) {
        strings.maxOrNull() ?: "fail"
    }
}