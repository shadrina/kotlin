interface Inv<T>
interface Cov<out T>
interface Contrav<in T>

interface A
interface B : A

<!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<A> with<B><!> fun c() {}

with<Inv<A>> with<Inv<B>> fun d() {}

<!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<Cov<A>> with<Cov<B>><!> fun e() {}

<!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<Contrav<A>> with<Contrav<B>><!> fun f() {}

<!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<Inv<T>> with<Inv<A>><!> fun <T> g() {}

<!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<Cov<T>> with<Cov<A>><!> fun <T> h() {}

<!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<Contrav<T>> with<Contrav<A>><!> fun <T> i() {}

<!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<T> with<B><!> fun <T : A> j() {}

with<T> with<B> fun <T : CharSequence> k() {}

<!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<A> with<A><!> fun l() {}