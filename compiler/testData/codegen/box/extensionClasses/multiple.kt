class O(val o: String)
class K(val k: String)

with<O>
with<K>
class R {
    fun result() = o + k
}

fun box() = with(O("O")) {
    with(K("K")) {
        R().result()
    }
}