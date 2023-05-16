//semigroupal涵盖了对context进行组合的概念, 利用Semigroupal和Functor可以允许对具有多个参数的函数进行排序。

// 示例1
// 组合两个Option

import cats.Semigroupal
import cats.instances.option._ // for Semigroupal

// 两个Option都为Some, 交出一个Some(tuple)
Semigroupal[Option].product(Some(123), Some("abc"))
// res1: Option[(Int, String)] = Some((123, "abc"))

// 其中有一个为None, 交出None
Semigroupal[Option].product(None, Some("abc"))
// res2: Option[Tuple2[Nothing, String]] = None
Semigroupal[Option].product(Some(123), None)
// res3: Option[Tuple2[Int, Nothing]] = None

// Semigroupal的伴生对象提供了从tuple2-tuple22的方法, 来将组合更多的context
Semigroupal.tuple3(Option(1), Option(2), Option(3))
// res4: Option[(Int, Int, Int)] = Some((1, 2, 3))
Semigroupal.tuple3(Option(1), Option(2), Option.empty[Int])
// res5: Option[(Int, Int, Int)] = None

// map2到map22的方法对2到22个上下文内的值应用一个用户指定的函数
Semigroupal.map3(Option(1), Option(2), Option(3))(_ + _ + _)
// res6: Option[Int] = Some(6)

Semigroupal.map2(Option(1), Option.empty[Int])(_ + _)
// res7: Option[Int] = None

// 还有contramap2到contramap22和imap2到imap22，它们分别需要Contravariant和Invariant的实例。

// Cats提供了一个叫apply的语法, 来简化上面Semigroupal的调用

import cats.instances.option._ // for Semigroupal
import cats.syntax.apply._ // for tupled and mapN

// 使用tupled方法从tuple中创建Option
(Option(123), Option("abc")).tupled // tupled is implicitly
// res8: Option[(Int, String)] = Some((123, "abc"))

// 同理, 对至多22个值都可以使用tupled方法
(Option(123), Option("abc"), Option(true)).tupled
// res9: Option[(Int, String, Boolean)] = Some((123, "abc", true))

// 除了tupled之外，Cats的apply语法还提供了一个叫做mapN的方法，它接受一个implicit的Functor和一个正确的函数来组合这些值。
final case class Cat(name: String, born: Int, color: String)

(
  Option("Garfield"),
  Option(1978),
  Option("Orange & black")
  ).mapN(Cat.apply)
// res10: Option[Cat] = Some(Cat("Garfield", 1978, "Orange & black"))

// 在上面提到的所有方法中，最常见的是使用mapN。
// 在内部，mapN使用Semigroupal从Option中提取数值，并使用Functor将这些数值应用到函数中。
val add: (Int, Int) => Int = (a, b) => a + b
// add: (Int, Int) => Int = <function2>

// 如果提供的函数的参数数量或类型不正确，会发生编译错误：
//(Option(1), Option(2), Option(3)).mapN(add)
// error: ':' expected but '(' found.
//   Option("Garfield"),
//         ^
// error: identifier expected but '}' found.

//(Option("cats"), Option(true)).mapN(add)
// error: ':' expected but '(' found.
//   Option("Garfield"),
//         ^
// error: identifier expected but '}' found.

(Option(5), Option(4)).mapN(add)

// Apply语法也有contramapN和imapN方法，接受Contravariant和Invariant functors。

import cats.Monoid
import cats.instances.int._ // for Monoid
import cats.instances.invariant._ // for Semigroupal
import cats.instances.list._ // for Monoid
import cats.instances.string._ // for Monoid
import cats.syntax.apply._ // for imapN

final case class Cat(
                      name: String,
                      yearOfBirth: Int,
                      favoriteFoods: List[String]
                    )

val tupleToCat: (String, Int, List[String]) => Cat =
  Cat.apply

val catToTuple: Cat => (String, Int, List[String]) =
  cat => (cat.name, cat.yearOfBirth, cat.favoriteFoods)

implicit val catMonoid: Monoid[Cat] = (
  Monoid[String],
  Monoid[Int],
  Monoid[List[String]]
  ).imapN(tupleToCat)(catToTuple)

import cats.syntax.semigroup._ // for |+|

val garfield   = Cat("Garfield", 1978, List("Lasagne"))
val heathcliff = Cat("Heathcliff", 1988, List("Junk Food"))

garfield |+| heathcliff
// res14: Cat = Cat("GarfieldHeathcliff", 3966, List("Lasagne", "Junk Food"))

// future semigroupal
import cats.instances.future._ // for Semigroupal
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

val futurePair = Semigroupal[Future].
  product(Future("Hello"), Future(123))

Await.result(futurePair, 1.second)
// res0: (String, Int) = ("Hello", 123)

// 这两个Futures在创建的时候就开始执行了，所以在调用product的时候它们已经在计算结果了。可以使用apply语法来压缩固定数量的Futures:

val futureCat = (
  Future("Garfield"),
  Future(1978),
  Future(List("Lasagne"))
  ).mapN(Cat.apply)

Await.result(futureCat, 1.second)
// res1: Cat = Cat("Garfield", 1978, List("Lasagne"))

// List semigroupal
import cats.instances.list._ // for Semigroupal

// 对两个List实例使用product, 得到的是笛卡尔积
Semigroupal[List].product(List(1, 2), List(3, 4))
// res2: List[(Int, Int)] = List((1, 3), (1, 4), (2, 3), (2, 4))

// Either semigroupal
import cats.instances.either._ // for Semigroupal

type ErrorOr[A] = Either[Vector[String], A]

Semigroupal[ErrorOr].product(Left(Vector("Error 1")), Left(Vector("Error 2")))
// res3: ErrorOr[Tuple2[Nothing, Nothing]] = Left(Vector("Error 1"))
// 和flatmap一样, Semigroupal的product方法并不能够积累所有的错误, 而是快速失败, 返回第一个错误

// 为什么List和Either的semigroupal和我们期望的行为不一样?
// Semigroupal[List].product(List(1, 2), List(3, 4)) != List(1, 2, 3, 4)
// Semigroupal[ErrorOr].product(Left(Vector("Error 1")), Left(Vector("Error 2"))) != Left(Vector("Error 1", "Error 2"))

// 这是因为List和Either同时也是一个Monad, 考虑下面的product实现:
import cats.Monad
import cats.syntax.functor._ // for map
import cats.syntax.flatMap._ // for flatmap

// 为什么是笛卡尔积的原因
def product[F[_]: Monad, A, B](fa: F[A], fb: F[B]): F[(A,B)] =
  fa.flatMap(a =>
    fb.map(b =>
      (a, b)
    )
  )

// for推导风格
def product[F[_]: Monad, A, B](fa: F[A], fb: F[B]): F[(A,B)] = for {
  a <- fa
  b <- fb
} yield (a, b)

// 如果因实现方式不同而对product有不同的语义，那就非常奇怪了。为了确保一致的语义，Cats'Monad（它扩展了Semigroupal）在map和flatMap方面提供了一个标准的product定义，正如上面所展示的。



