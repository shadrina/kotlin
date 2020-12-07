interface Inv<T>
interface Cov<out T>
interface Contrav<in T>

interface A
interface B : A

<!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<A> with<B><!> class C

with<Inv<A>> with<Inv<B>> class D

<!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<Cov<A>> with<Cov<B>><!> class E

<!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<Contrav<A>> with<Contrav<B>><!> class F

<!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<Inv<T>> with<Inv<A>><!> class G<T>

<!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<Cov<T>> with<Cov<A>><!> class H<T>

<!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<Contrav<T>> with<Contrav<A>><!> class I<T>

<!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<T> with<B><!> class J<T : A>

with<T> with<B> class K<T : CharSequence>

<!SUBTYPING_BETWEEN_ADDITIONAL_RECEIVERS!>with<A> with<A><!> class L