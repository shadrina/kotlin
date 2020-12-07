interface Inv<T>
interface Cov<out T>
interface Contrav<in T>

interface A
interface B : A

with<A> with<B> fun c() {}

with<Inv<A>> with<Inv<B>> fun d() {}

with<Cov<A>> with<Cov<B>> fun e() {}

with<Contrav<A>> with<Contrav<B>> fun f() {}

with<Inv<T>> with<Inv<A>> fun <T> g() {}

with<Cov<T>> with<Cov<A>> fun <T> h() {}

with<Contrav<T>> with<Contrav<A>> fun <T> i() {}

with<T> with<B> fun <T : A> j() {}

with<T> with<B> fun <T : CharSequence> k() {}

with<A> with<A> fun l() {}