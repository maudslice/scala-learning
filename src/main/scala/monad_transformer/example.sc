// cats为各种各样的monad提供的transformer, 这些transformer提供了额外的细节, 用于把当前monad和其他的monad进行组合
// cats提供的transformer每个都以T为后缀命名：EitherT将Either与其他单体组合，OptionT将Option组合，等等
// 总的来说, 使用monad transformer可以在处理嵌套单体的 "堆栈 "时不需要嵌套的理解和模式匹配。
// 每个monad transformer，如FutureT、OptionT或EitherT，都提供了将其相关monad与其他monad合并所需的代码。transformer是一个包裹monad堆栈的数据结构，为其配备map和flatMap方法，用于解包和重新打包整个堆栈。
//monad transformer的类型签名是由内向外写的，所以EitherT[Option, String, A]是Option[Either[String, A]]的包装器。在为深度嵌套的单体编写转换器类型时，使用类型别名通常很有用。
// 示例1

import cats.data.OptionT

// 组合List和Option
type ListOption[A] = OptionT[List, A]

import cats.instances.list._     // for Monad
import cats.syntax.applicative._ // for pure

// 使用OptionT提供的apply方法构造实例
val result1: ListOption[Int] = OptionT(List(Option(10)))
// result1: ListOption[Int] = OptionT(List(Some(10)))

// 使用pure方法构造实例
val result2: ListOption[Int] = 32.pure[ListOption]
// result2: ListOption[Int] = OptionT(List(Some(32)))

// 合并两个ListOption
// transformer的map, flatmap方法可以直接对stack最里面的类型进行访问, 在这个例子中, 他们是Int
result1.flatMap{x =>
  result2.map(y => x + y)
}
//val res0: cats.data.OptionT[List,Int] = OptionT(List(Some(42)))

// 示例2
// 如果想得到一个Either和Option的组合:Either[String, Option[A]], Option只有一个类型参数, 所以可以直接用OptionT, 但Either有两个类型参数, 所以需要一个中间类型来向transformer中传入正确的类型参数
// Alias Either to a type constructor with one parameter:
type ErrorOr[A] = Either[String, A]

// Build our final monad stack using OptionT:
type ErrorOrOption[A] = OptionT[ErrorOr, A]

import cats.instances.either._ // for Monad

val a = 10.pure[ErrorOrOption]
// a: ErrorOrOption[Int] = OptionT(Right(Some(10)))
val b = 32.pure[ErrorOrOption]
// b: ErrorOrOption[Int] = OptionT(Right(Some(32)))

val c = a.flatMap(x => b.map(y => x + y))
// c: OptionT[ErrorOr, Int] = OptionT(Right(Some(42)))

// 示例3
// 创建一个三个monad的组合
//
import scala.concurrent.Future
import cats.data.{EitherT, OptionT}

type FutureEither[A] = EitherT[Future, String, A]

type FutureEitherOption[A] = OptionT[FutureEither, A]

import cats.instances.future._ // for Monad
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

// 现在, FutureEitherOption由三个Monad嵌套组成, 但可以使用flatmap map来直接访问他们包裹的值
val futureEitherOr: FutureEitherOption[Int] =
  for {
    a <- 10.pure[FutureEitherOption]
    b <- 32.pure[FutureEitherOption]
  } yield a + b

// 示例4
// 使用transformer.value方法对monad嵌套的堆栈
// Create using apply:
val errorStack1 = OptionT[ErrorOr, Int](Right(Some(10)))
// errorStack1: OptionT[ErrorOr, Int] = OptionT(Right(Some(10)))

// Create using pure:
val errorStack2 = 32.pure[ErrorOrOption]
// errorStack2: ErrorOrOption[Int] = OptionT(Right(Some(32)))

// Extracting the untransformed monad stack:
errorStack1.value
// res4: ErrorOr[Option[Int]] = Right(Some(10))

// Mapping over the Either in the stack:
errorStack2.value.map(_.getOrElse(-1))
// res5: Either[String, Int] = Right(32)

// 每调用一次value方法对monad嵌套堆栈弹出一层
futureEitherOr
// res6: FutureEitherOption[Int] = OptionT(
//   EitherT(Future(Success(Right(Some(42)))))
// )

val intermediate = futureEitherOr.value
// intermediate: FutureEither[Option[Int]] = EitherT(
//   Future(Success(Right(Some(42))))
// )

val stack = intermediate.value
// stack: Future[Either[String, Option[Int]]] = Future(Success(Right(Some(42))))

Await.result(stack, 1.second)
// res7: Either[String, Option[Int]] = Right(Some(42))