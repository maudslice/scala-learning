// 自定义monad需要实现3个方法, pure, flatmap, tailRecM
// Option Monad 例子

import cats.Monad
import scala.annotation.tailrec

val optionMonad = new Monad[Option] {
  def flatMap[A, B](opt: Option[A])(fn: A => Option[B]): Option[B] =
    opt flatMap fn

  def pure[A](opt: A): Option[A] =
    Some(opt)

  /**
   * tailRecM方法是Cats中使用的一种优化方法，用于限制嵌套调用flatMap所消耗的堆栈空间。该方法应递归调用自己，直到fn的结果返回一个右。
   */
  @tailrec
  def tailRecM[A, B](a: A)(fn: A => Option[Either[A, B]]): Option[B] =
    fn(a) match {
      case None => None
      case Some(Left(a1)) => tailRecM(a1)(fn)
      case Some(Right(b)) => Some(b)
    }
}

import cats.syntax.flatMap._ // For flatMap

//假设想写一个方法来调用一个函数，直到该函数表明它应该停止。
def retry[F[_] : Monad, A](start: A)(f: A => F[A]): F[A] =
  f(start).flatMap { a =>
    retry(a)(f)
  }

// retry不是堆栈安全的

import cats.syntax.all._
import cats.instances.option._

retry(100)(a => if (a == 0) None else Some(a - 1))
//retry(100000)(a => if (a == 0) None else Some(a - 1)) //StackOverflowError

// 使用tailRecM实现
def retryTailRecM[F[_] : Monad, A](start: A)(f: A => F[A]): F[A] =
  Monad[F].tailRecM(start) { a =>
    f(a).map(a2 => Left(a2))
  }

retryTailRecM(100000)(a => if(a == 0) None else Some(a - 1))
// val res1: Option[Int] = None

// tailRecM方法必须明确调用, 没有一种代码可以把非尾递归的调用转换成尾递归调用
// 但是有几个Monad类型提供的实用工具, 可以更容易的编写这类方法
import cats.syntax.monad._ // for iterateWhileM

def retryM[F[_]: Monad, A](start: A)(f: A => F[A]): F[A] =
  start.iterateWhileM(f)(a => true)

retryM(100000)(a => if(a == 0) None else Some(a - 1))
