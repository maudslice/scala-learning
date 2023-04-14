// foldLeft和foldRight是灵活的迭代方法，但它们需要做大量的工作来定义累加器和组合器函数
// Traverse类型类是一个更高层次的工具，它利用Applicatives为迭代提供一个更方便、更合法的模式。

// 下面使用Scala标准库中的Future.traverse和Future.sequence方法演示Traverse。这些方法提供了针对Future的遍历模式的实现。

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

val hostnames = List(
  "alpha.example.com",
  "beta.example.com",
  "gamma.demo.com"
)

def getUptime(hostname: String): Future[Int] =
  Future(hostname.length * 60) // just for demonstration

val allUptimes: Future[List[Int]] =
  hostnames.foldLeft(Future(List.empty[Int])) {
    (accum, host) =>
      val uptime = getUptime(host)
      for {
        accum <- accum
        uptime <- uptime
      } yield accum :+ uptime
  }

Await.result(allUptimes, 1.second)
// res0: List[Int] = List(1020, 960, 840)

// 在allUptimes中, 每次迭代都要创建和组合Future, 稍显复杂
// 下面使用Future.traverse来对上面的代码进行改进, 它正是为这种模式量身定做的
val allUptimes2: Future[List[Int]] = {
  // traverse: List[A] => Future[List[B]]
  Future.traverse(hostnames)(getUptime)
}

// 忽略executor和canBuildFrom,  Future.traverse的实现就和上面的allUptimes类似:
/*
def traverse[A, B](values: List[A])
    (func: A => Future[B]): Future[List[B]] =
  values.foldLeft(Future(List.empty[B])) { (accum, host) =>
    val item = func(host)
    for {
      accum <- accum
      item  <- item
    } yield accum :+ item
  }
 */

Await.result(allUptimes2, 1.second)
// res2: List[Int] = List(1020, 960, 840)

// Future.traverse抽象化了fold和定义累加器和组合函数的麻烦。它给了一个干净的高级接口来做想要的事情：
// 以一个List[A]开始
// 提供一个A => Future[B]的函数
// 以一个Future[List[B]]结束

// 标准库中的另一个功能类似的方法Future.sequence, 它假定我们从一个List[Future[B]]开始，不需要提供一个identity函数
/*
object Future {
  def sequence[B](futures: List[Future[B]]): Future[List[B]] =
    traverse(futures)(identity)

  // etc...
}
 */
// 从一个List[Future[A]]开始
// 以一个Future[List[A]]结束

// Future.traverse和Future.sequence解决了一个非常具体的问题：它们允许我们在一个Future序列上进行迭代，并累积一个结果。上面的简化例子只适用于Lists，但真正的Future.traverse和Future.sequence适用于任何标准的Scala集合。
//
//Cats的Traverse typeclass概括了这些模式，可以与任何类型的Applicative一起工作：Future、Option、Validated等等。

// 考虑使用Applicative实现上面的traverse

import cats.Applicative
import cats.instances.future._ // for Applicative
import cats.syntax.applicative._ // for pure

List.empty[Int].pure[Future]

def oldCombine(
                accum: Future[List[Int]],
                host: String
              ): Future[List[Int]] = {
  val uptime = getUptime(host)
  for {
    accum <- accum
    uptime <- uptime
  } yield accum :+ uptime
}

// 现在等同于使用Semigroupal.combine
// Combining accumulator and hostname using an Applicative:

import cats.syntax.apply._ // for mapN

def newCombine(accum: Future[List[Int]],
               host: String): Future[List[Int]] =
  (accum, getUptime(host)).mapN(_ :+ _)

// 对newCombine进行泛化, 让它可以和任何Applicative一起工作
def listTraverse[F[_]: Applicative, A, B]
(list: List[A])(func: A => F[B]): F[List[B]] =
  list.foldLeft(List.empty[B].pure[F]) { (accum, item) =>
    (accum, func(item)).mapN(_ :+ _)
  }

def listSequence[F[_]: Applicative, B]
(list: List[F[B]]): F[List[B]] =
  listTraverse(list)(identity)

// 使用listTraverse实现上面upTime的例子
val totalUptime = listTraverse(hostnames)(getUptime)

Await.result(totalUptime, 1.second)
// res5: List[Int] = List(1020, 960, 840)