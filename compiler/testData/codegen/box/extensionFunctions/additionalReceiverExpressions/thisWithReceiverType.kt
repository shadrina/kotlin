class O(val o: String = "O")
class K(val k: String = "K")

fun f(o: O, k: K) with(o) with(k) = this<O>.o + this<K>.k

fun box() = f(O(), K())