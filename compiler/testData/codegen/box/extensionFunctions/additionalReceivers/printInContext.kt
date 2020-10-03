interface Context {
    fun result(): String
}

object OkContext : Context {
    override fun result() = "OK"
}

with<Context> fun <T> T.print() = toString() + result()

fun box() = with(OkContext) { "".print() }