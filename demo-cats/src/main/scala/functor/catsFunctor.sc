import cats.instances.function._
import cats.syntax.functor._

// for map

val f1 = (x: Int) => x.toDouble
val f2 = (y: Double) => y * 2

val f3: Int => Double = f1 map f2 // 使用map组合两个函数
(f1 map f2) (1)
val f4: Int => Double = f1 andThen f2 // 使用andThen组合两个函数
(f1 andThen f2) (1)
f2(f1(1)) //手动组合两个函数

// 函数组合就是一个sequence操作, 每次使用map在调用链上追加一个新的操作, 实际上调用func并不会有任何计算被执行, 直到给最后的函数func传递一个参数, 所有的操作会按照顺序依次执行
val func =
  ((x: Int) => x.toDouble).
    map(x => x + 1).
    map(x => x * 2).
    map(x => s"$x!")

func(123)

// functor typeclass and instance

import cats.Functor
import cats.instances.list._ // for Functor
import cats.instances.option._ // for Functor

val list1 = List(1, 2, 3)
val list2 = Functor[List].map(list1)(_ * 2)

val option1 = Option(123)
val option2 = Functor[Option].map(option1)(_.toString)

// lift 操作
val func2 = (x: Int) => x + 1
// Int => Int lifted => Option[Int] => Option[Int]
val liftFunc = Functor[Option].lift(func2)
Functor[List].as(list1, "as")

// 使用高阶类型参数
def doMath[F[_]](start: F[Int])(implicit functor: Functor[F]): F[Int] =
  start.map(_ * 2 + 1)

//import cats.instances.option._ // for Functor
//import cats.instances.list._   // for Functor

doMath(Option(20))
doMath(List(1, 2, 3, 4))

// 自定义functor: 实现map方法
implicit val optionFunctor: Functor[Option] = new Functor[Option] {
  override def map[A, B](fa: Option[A])(f: A => B) = fa.map(f)
}


// 当需要额外的参数时, 可以将typeclass instance定义为implicit def
import scala.concurrent.{Future, ExecutionContext}

implicit def futureFunctor
(implicit ec: ExecutionContext): Functor[Future] =
  new Functor[Future] {
    def map[A, B](value: Future[A])(func: A => B): Future[B] =
      value.map(func)
  }

// 每当直接或间接调用futureFunctor时, 编译器会隐式定位并在调用的地方搜索隐式参数Functor[Future], 并递归搜索一个ExecutionContext
// We write this:
implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
Functor[Future]

// The compiler expands to this first:
Functor[Future](futureFunctor)

// And then to this:
Functor[Future](futureFunctor(ec))
