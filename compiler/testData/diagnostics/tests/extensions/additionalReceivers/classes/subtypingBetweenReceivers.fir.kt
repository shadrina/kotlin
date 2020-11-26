interface Inv<T>
interface Cov<out T>
interface Contrav<in T>

interface A
interface B : A

with<A> with<B> class C

with<Inv<A>> with<Inv<B>> class D

with<Cov<A>> with<Cov<B>> class E

with<Contrav<A>> with<Contrav<B>> class F

with<Inv<T>> with<Inv<A>> class G<T>

with<Cov<T>> with<Cov<A>> class H<T>

with<Contrav<T>> with<Contrav<A>> class I<T>

with<T> with<B> class J<T : A>

with<T> with<B> class K<T : CharSequence>

with<A> with<A> class L