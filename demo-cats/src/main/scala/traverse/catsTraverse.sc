import cats.{Applicative, Traverse}
import cats.syntax.apply._
import cats.syntax.applicative._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future} // for pure


def listTraverse[F[_]: Applicative, A, B]
(list: List[A])(func: A => F[B]): F[List[B]] =
  list.foldLeft(List.empty[B].pure[F]) { (accum, item) =>
    (accum, func(item)).mapN(_ :+ _)
  }

def listSequence[F[_]: Applicative, B]
(list: List[F[B]]): F[List[B]] =
  listTraverse(list)(identity)

val hostnames = List(
  "alpha.example.com",
  "beta.example.com",
  "gamma.demo.com"
)

def getUptime(hostname: String): Future[Int] =
  Future(hostname.length * 60) // just for demonstration

// 上面使用applicative自定义实现的traverse和sequence只能对List生效, 下面是cats的Traverse的简略实现, traverse和sequence可以对任意的序列生效
/*
trait Traverse[F[_]] {
  def traverse[G[_]: Applicative, A, B]
      (inputs: F[A])(func: A => G[B]): G[F[B]]

  def sequence[G[_]: Applicative, B]
      (inputs: F[G[B]]): G[F[B]] =
    traverse(inputs)(identity)
}
 */
// 使用示例:
val totalUptime: Future[List[Int]] =
Traverse[List].traverse(hostnames)(getUptime)

Await.result(totalUptime, 1.second)
// res0: List[Int] = List(1020, 960, 840)

val numbers = List(Future(1), Future(2), Future(3))

val numbers2: Future[List[Int]] =
  Traverse[List].sequence(numbers)

Await.result(numbers2, 1.second)
// res1: List[Int] = List(1, 2, 3)

// 语法版本的方法:
import cats.syntax.traverse._ // for sequence and traverse

// scala3 编译器有bug吧, 总之先注释了, 在scala2中可以编译
//Await.result(hostnames.traverse(getUptime), 1.second)
// res2: List[Int] = List(1020, 960, 840)
//Await.result(numbers.sequence, 1.second)
// res3: List[Int] = List(1, 2, 3)