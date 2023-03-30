import cats.Monad
import cats.instances.option._ // for Monad
import cats.instances.list._ // for Monad

val opt1 = Monad[Option].pure(3)
val opt2 = Monad[Option].flatMap(opt1)(a => Some(a + 2))
val opt3 = Monad[Option].map(opt2)(a => a * 100)

val list1 = Monad[List].pure(3)
val list2 = Monad[List].flatMap(List(1, 2, 3))(a => List(a, a * 10))
val list3 = Monad[List].map(list2)(_ + 10)

// cats 提供了标准库中所有monads的实例的实现

import cats.instances.option._ // for Monad

Monad[Option].flatMap(Option(1))(a => Option(a * 2))
// res0: Option[Int] = Some(2)

import cats.instances.list._ // for Monad

Monad[List].flatMap(List(1, 2, 3))(a => List(a, a * 10))
// res1: List[Int] = List(1, 10, 2, 20, 3, 30)

import cats.instances.vector._ // for Monad

Monad[Vector].flatMap(Vector(1, 2, 3))(a => Vector(a, a * 10))
// res2: Vector[Int] = Vector(1, 10, 2, 20, 3, 30)

//cats 为Future也提供的Monad实例的实现, 但与Future类中的方法不同的是, Monad[Future]的pure和flatmap方法不能接受隐式的ExecutionContext 参数, 因为它不是声明在trait Monad中的
// 所以, 在使用Monad[Future]的时候, 需要在范围有一个ExecutionContext

import cats.instances.future._ // for Monad
import scala.concurrent._
import scala.concurrent.duration._

//val fm = Monad[Future] // not compile
import scala.concurrent.ExecutionContext.Implicits.global

// Monad会捕获范围内的ExecutionContext实例, 用于pure和flatmap
val fm = Monad[Future]
val future = fm.flatMap(fm.pure(1))(x => fm.pure(x + 2))

Await.result(future, 1.second)

// use syntax

import cats.instances.option._ // for Monad
import cats.instances.list._ // for Monad
import cats.syntax.applicative._ // for pure

// 在通过这种方式构造一个Monad的时候, 需要通过类型参数来获得指定的实例
1.pure[Option]
1.pure[List]

// 很难在List或者Option这样的scala Monad上演示flatmap和map方法, 因为它们自己对map和flatmap有明确的定义
// 编写一个通用的函数来对Monad进行演示:

import cats.syntax.functor._ // for map
import cats.syntax.flatMap._ // for flatMap

def sumSquare[F[_] : Monad](a: F[Int], b: F[Int]): F[Int] =
  a.flatMap(x => b.map(y => x * x + y * y))

sumSquare(Option(3), Option(4))
sumSquare(List(1, 2, 3), List(4, 5))

// 也可以使用for推导式来实现上面的代码, 编译器会自动插入需要的隐式转换
def sumSquare[F[_] : Monad](a: F[Int], b: F[Int]): F[Int] =
  for {
    ax <- a
    bx <- b
  } yield ax * ax + bx * bx

sumSquare(Option(3), Option(4))
sumSquare(List(1, 2, 3), List(4, 5))
