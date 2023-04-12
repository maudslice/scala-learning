import cats.Semigroupal
import cats.implicits.catsSyntaxTuple2Semigroupal
import cats.instances.either._ // for Semigroupal

type ErrorOr[A] = Either[Vector[String], A]
val error1: ErrorOr[Int] = Left(Vector("Error 1"))
val error2: ErrorOr[Int] = Left(Vector("Error 2"))

Semigroupal[ErrorOr].product(error1, error2)
// res0: ErrorOr[(Int, Int)] = Left(Vector("Error 1"))
// 为了保持和Either Monad的语义一致, 所以Either Semigroupal并不能收集错误, 而是返回第一个错误

// 为了收集所有的错误，只需将tupled替换为其 "parallel "版本，称为parTupled。
import cats.syntax.parallel._ // for parTupled

(error1, error2).parTupled
// res2: ErrorOr[(Int, Int)] = Left(Vector("Error 1", "Error 2"))
// 这种行为并不是使用Vector作为错误类型的特殊情况。任何有Semigroup实例的类型都可以工作。例如，下面用List代替。

import cats.instances.list._ // for Semigroup on List

type ErrorOrList[A] = Either[List[String], A]
val errStr1: ErrorOrList[Int] = Left(List("error 1"))
val errStr2: ErrorOrList[Int] = Left(List("error 2"))

(errStr1, errStr2).parTupled
// res3: ErrorOrList[(Int, Int)] = Left(List("error 1", "error 2"))

// Parallel为Semigroupal和相关类型的方法提供了很多语法方法，但最常用的是parMapN。下面是一个在错误处理情况下使用parMapN的例子
val success1: ErrorOr[Int] = Right(1)
val success2: ErrorOr[Int] = Right(2)
val addTwo = (x: Int, y: Int) => x + y

(error1, error2).parMapN(addTwo)
// res4: ErrorOr[Int] = Left(Vector("Error 1", "Error 2"))
(success1, success2).parMapN(addTwo)
// res5: ErrorOr[Int] = Right(3)

// Parallel是如何工作的?
/*
trait Parallel[M[_]] {
  // 如果存在类型构造器M的Parallel实例, 那么:
  //    M一定有一个Monad实例
  //    一定有一个相关的类型构造函数F, 它有一个Applicative的实例, 并且, 可以把M转换为F
  //    ~>是FunctionK的类型别名, FunctionK[M, F]是一个将M[A]转换到F[A]的函数
  type F[_]

  def applicative: Applicative[F]
  def monad: Monad[M]
  def parallel: ~>[M, F]
}
 */

// 定义一个将Option转换为List的FunctionK
import cats.arrow.FunctionK

object optionToList extends FunctionK[Option, List] {
  def apply[A](fa: Option[A]): List[A] =
    fa match {
      case None    => List.empty[A]
      case Some(a) => List(a)
    }
}

optionToList(Some(1))
// res6: List[Int] = List(1)
optionToList(None)
// res7: List[Nothing] = List()

// 由于类型参数A是泛型的，FunctionK不能检查类型构造函数M所包含的任何值，转换必须纯粹以类型构造函数M和F的结构进行。
//所以总的来说，Parallel允许把一个有Monad实例的类型，转换为一些有Applicative（或Semigroupal）实例的相关类型。这个相关的类型会有一些有用的替代语义。在上面的例子中，Either的相关应用型允许累积错误而不是故障快速的语义。

// 最后, 用parTupled来实现"parallel"语义的product(List)
(List(1, 2), List(3, 4)).tupled
// res8: List[(Int, Int)] = List((1, 3), (1, 4), (2, 3), (2, 4))
(List(1, 2), List(3, 4)).parTupled
// res9: List[(Int, Int)] = List((1, 3), (2, 4))