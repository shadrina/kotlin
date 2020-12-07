interface Inv<T>
interface Cov<out T>
interface Contrav<in T>

interface A
interface B : A

<!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<A> with<B><!> val c get() = null

with<Inv<A>> with<Inv<B>> val d get() = null

<!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<Cov<A>> with<Cov<B>><!> val e get() = null

<!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<Contrav<A>> with<Contrav<B>><!> val f get() = null

class Some<T> {
    <!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<Inv<T>> with<Inv<A>><!> val g get() = null

    <!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<Cov<T>> with<Cov<A>><!> val h get() = null

    <!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<Contrav<T>> with<Contrav<A>><!> val i get() = null

    <!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<T> with<B><!> val j get() = null
}

class Bounded<T : CharSequence> {
    with<T> with<B> val k get() = null
}

<!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<A> with<A><!> val l get() = null