import cats.{Functor, Semigroupal}

// 在更广泛的函数式编程文献中，Semigroupals并不经常被提及。它们提供了一个相关类型类的功能子集，这个类型类被称为applicative functor（简称 "applicative"）。
//Semigroupal和Applicative有效地提供了连接上下文的同一概念的替代编码。这两种编码都是由Conor McBride和Ross Paterson在2008年的同一篇论文中介绍的。
//Cats使用两个类型类对Applicative进行建模。第一个是cats.Apply，扩展了Semigroupal和Functor，并增加了一个ap方法，用于将一个参数应用到上下文中的一个函数。第二个是cats.Applicative，扩展了Apply并增加了第四章中介绍的pure方法。下面是代码中的一个简化定义：
trait Apply[F[_]] extends Semigroupal[F] with Functor[F] {
  // ap将fa应用于上下文中的一个函数ff
  def ap[A, B](ff: F[A => B])(fa: F[A]): F[B]

  // Semigroupal的product方法是用ap和map来定义的
  // product, ap和map之间有一种紧密的联系, 允许用其中的任意一个来定义其他两个
  def product[A, B](fa: F[A], fb: F[B]): F[(A, B)] =
    ap(map(fa)(a => (b: B) => (a, b)))(fb)
}

trait Applicative[F[_]] extends Apply[F] {
  // Applicative引入了pure方法, 和Monad中的pure相同, 它从一个解包的值中构造一个新的Applicative实例, 在这个意义上, Applicative和Apply的关系就像是Monoid和Semigroup的关系
  def pure[A](a: A): F[A]
}

/*
在cats中涉及到的typeclass的继承树:
      Monad extends Applicative(定义了pure) with FlatMap(定义了flatmap)
      Applicative extends Apply(定义了ap)
      FlatMap extends Apply(定义了ap)
      Apply extends Semigroupal(定义了product) with Functor(定义了map)

      层次结构中的每个类型类都代表了一组特定的排序语义，引入了一组特征方法，并以这些方法定义了其超类型的功能：

      每个Monad都是一个Applicative；
      每个Applicative都是一个Semigroupal；
      等等。

      由于类型类之间关系的规律性，继承关系在一个类型类的所有实例中是不变的。Apply用ap和map来定义product；Monad用pure和flatMap来定义product、ap和map。
 */

// 总结
// 虽然monads和functors是在本书中所涉及的最广泛使用的排序数据类型，但Semigroupal和Applicative是最通用的。这些类型类提供了一种通用的机制，可以在上下文中组合值和应用函数，可以从这些类型中形成Monad和各种其他组合器。
//Semigroupal和Applicative最常被用作组合独立值的手段，如验证规则的结果。Cats为这一特定目的提供了Validated类型，以及apply语法，作为表达规则组合的一种方便方式。