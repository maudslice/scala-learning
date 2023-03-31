def slowly[A](body: => A) =
  try body finally Thread.sleep(100)

def factorial(n: Int): Int = {
  val ans = slowly(if (n == 0) 1 else n * factorial(n - 1))
  println(s"fact $n $ans")
  ans
}

// 运行时打印步骤的factorial函数
factorial(5)
// fact 0 1
// fact 1 1
// fact 2 2
// fact 3 6
// fact 4 24
// fact 5 120
// res9: Int = 120

// 对factorial进行并行计算

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._

Await.result(Future.sequence(Vector(
  Future(factorial(5)),
  Future(factorial(5))
)), 5.seconds)
// 无法看出每个步骤的信息是来自哪个Future
// fact 0 1
// fact 0 1
// fact 1 1
// fact 1 1
// fact 2 2
// fact 2 2
// fact 3 6
// fact 3 6
// fact 4 24
// fact 4 24
// fact 5 120
// fact 5 120
// res: scala.collection.immutable.Vector[Int] =
//   Vector(120, 120)

// 重写factorial，使其在Writer中捕获日志信息。证明这使Writer能够可靠地分离并发计算的日志。

import cats.data.Writer
import cats.syntax.writer._
import cats.syntax.applicative._

type Logged[A] = Writer[Vector[String], A]
def factorial2(n: Int): Logged[Int] =
  for {
    ans <- if (n == 0) 1.pure[Logged] else slowly(factorial2(n - 1).map(_ * n))
    _ <- Vector(s"fact $n $ans").tell
  } yield ans

val (log, res) = factorial2(5).run
// log: Vector[String] = Vector(
//   "fact 0 1",
//   "fact 1 1",
//   "fact 2 2",
//   "fact 3 6",
//   "fact 4 24",
//   "fact 5 120"
// )
// res: Int = 120

// 并行运行几个阶乘，独立捕捉它们的日志，而不必担心交错
Await.result(Future.sequence(Vector(
  Future(factorial2(5)),
  Future(factorial2(5)),
)).map(_.map(_.written)), 5.seconds)