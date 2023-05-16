
trait Check[E, A] {
  // 使用semigroup来合并两个error, 这里不使用monoid是因为不需要identity elem, 应该让我们的约束尽可能的小
  def and(that: Check[E, A]): Check[E, A] =
    ???

  // other methods...
}

// 至少有两种实现and的方式
// 第一种, 将check表示为函数, Check数据类型成为一个提供组合方法的函数的简单封装
// 为了消除歧义, 下面把这种实现称为CheckF

import cats.Semigroup
import cats.syntax.either._ // for asLeft and asRight
import cats.syntax.semigroup._ // for |+|

final case class CheckF[E, A](f: A => Either[E, A]) {
  def apply(a: A): Either[E, A] = f(a)

  def and(that: CheckF[E, A])(implicit s: Semigroup[E]): CheckF[E, A] =
    CheckF { a =>
      (this (a), that(a)) match {
        case (Left(e1), Left(e2)) => (e1 |+| e2).asLeft
        case (Left(e1), Right(_)) => e1.asLeft
        case (Right(_), Left(e2)) => e2.asLeft
        case (Right(_), Right(_)) => a.asRight
      }
    }
}

// 使用CheckF

import cats.instances.list._ // for Semigroup

val a: CheckF[List[String], Int] =
  CheckF { v =>
    if (v > 2) v.asRight
    else List("Must be > 2").asLeft
  }

val b: CheckF[List[String], Int] =
  CheckF { v =>
    if (v < -2) v.asRight
    else List("Must be < -2").asLeft
  }

val check: CheckF[List[String], Int] =
  a and b

check(5)
// val res0: Either[List[String],Int] = Left(List(Must be < -2))
check(0)
// val res1: Either[List[String],Int] = Left(List(Must be > 2, Must be < -2))

// 如果试图创建CheckF，却以无法积累的类型表示error，会发生什么？例如，Nothing就没有Semigroup实例。如果创建CheckF[Nothing, A]的实例会怎样？
val a: CheckF[Nothing, Int] =
  CheckF(v => v.asRight)

val b: CheckF[Nothing, Int] =
  CheckF(v => v.asRight)

//val check = a and b
// compile error: No implicits found for parameter s: Semigroup[Nothing]
// 发生了编译错误, 因为Nothing并没有Semigroup实例, 这正是我们所期望的

// 第二种, 将Check建模为一个代数数据类型(ADT), 每个组合器都有一个显式的数据类型, 下面这种实现名为为Check

sealed trait Check[E, A] {

  import Check._

  def and(that: Check[E, A]): Check[E, A] = And(this, that)

  def apply(a: A)(implicit s: Semigroup[E]): Either[E, A] = this match {
    case Pure(f) => f(a)
    case And(l, r) =>
      (l(a), r(a)) match {
        case (Left(e1), Left(e2)) => (e1 |+| e2).asLeft
        case (Left(e1), Right(_)) => e1.asLeft
        case (Right(_), Left(e2)) => e2.asLeft
        case (Right(_), Right(_)) => a.asRight
      }
  }

}

object Check {
  final case class And[E, A](left: Check[E, A], right: Check[E, A]) extends Check[E, A]

  final case class Pure[E, A](f: A => Either[E, A]) extends Check[E, A]

  def pure[E, A](f: A => Either[E, A]): Check[E, A] = Pure(f)
}

// 使用Check
val a: Check[List[String], Int] =
  Check.pure(v => if (v > 2) v.asRight else List("Must be > 2").asLeft)
val b: Check[List[String], Int] =
  Check.pure(v => if (v < -2) v.asRight else List("Must be < -2").asLeft)

val check: Check[List[String], Int] = a and b

check(0)
check(5)

// 虽然ADT的实现比函数包装器的实现更加冗长，但它的优点是将计算的结构（我们创建的ADT实例）与赋予它意义的过程（apply方法）干净地分开。从这里我们有很多选择：
//
//在创建后检查和重构检查；
//将apply的 "实现 "移出到自己的模块中；
//实现提供其他功能的替代解释器（例如，可视化检查）。
//由于其灵活性，后面的部分将使用ADT实现

// 对And的apply的实现使用的是applicative functors的pattern 。Either有一个Applicative实例，但它并没有我们想要的语义。它是快速失败的，而不会积累错误。
//如果想积累错误，Validated是一个更合适的抽象。作为回报，有更多的代码可以被重用 ,因为可以在apply的实现中可以使用Validated的apply方法。

import cats.data.Validated
import cats.syntax.apply._ // for mapN
import cats.data.Validated._ // for Valid and Invalid

sealed trait CheckV[E, A] {

  import CheckV._

  def and(that: CheckV[E, A]): CheckV[E, A] =
    And(this, that)

  def or(that: CheckV[E, A]): CheckV[E, A] =
    Or(this, that)

  def apply(a: A)(implicit s: Semigroup[E]): Validated[E, A] =
    this match {
      case Pure(func) => func(a)
      case And(left, right) =>
        (left(a), right(a)).mapN((_, _) => a)
      case Or(left, right) =>
        left(a) match {
          case Valid(_) => Valid(a)
          case Invalid(e1) =>
            right(a) match {
              case Valid(_) => Valid(a)
              case Invalid(e2) => Invalid(e1 |+| e2)
            }
        }

    }
}

object CheckV {
  final case class And[E, A](left: CheckV[E, A], right: CheckV[E, A]) extends CheckV[E, A]

  final case class Pure[E, A](func: A => Validated[E, A]) extends CheckV[E, A]

  final case class Or[E, A](left: CheckV[E, A], right: CheckV[E, A]) extends CheckV[E, A]
}

// 考虑下面的场景: 如何让Check能够进行数据转换? 如解析输入
// 如果从map考虑:
// type Check[E, A] = A => Either[E, A], 这显然是行不通的, 因为Check的定义要求数据和输出的类型相同
// def map(check: Check[E, A])(func: A => B): Check[E, ???] map的结果应该是什么类型? 显然不可能是A或B
// 为了实现map, 需要改变Check的定义, 具体来说, 需要一个新的类型变量来区分输入和输出:
// type Check[E, A, B] = A => Either[E, B]

// Check现在可以表示诸如将一个字符串解析为一个Int的操作:
//val parseInt: Check[List[String], String, Int] = ???
// etc...

// 然而，按照上面的定义拆分输入和输出类型会引起另一个问题。到目前为止，我们一直在假设一个Check在成功时总是返回其输入。我们在and和or中使用了这一点，忽略了左右规则的输出，只是在成功时返回原始输入：
/*
(this(a), that(a)) match {
  case And(left, right) =>
    (left(a), right(a))
      .mapN((result1, result2) => Right(a))

  // etc...
}
 */
// 如果按照新的Check定义, 那么就不能返回Right(a), 因为他的类型是Either[E, A]而不是Either[E, B], 返回的时候必须在Right(result1)和Right(result2)之间选择一个, 由此可以得出, 上面的抽象是错误的

// 但我们可以通过区分"前提"和"检查"的概念来取得进展, 前者可以用逻辑运算来组合, 比如And和Or, 后者可以转换数据
// 下面是"前提"的定义, 如果一个前提成功的话, 返回其输入:
sealed trait Predicate[E, A] {
  def and(that: Predicate[E, A]): Predicate[E, A] =
    AndP(this, that)

  def or(that: Predicate[E, A]): Predicate[E, A] =
    OrP(this, that)

  def apply(a: A)(implicit s: Semigroup[E]): Validated[E, A] =
    this match {
      case PureP(func) =>
        func(a)

      case AndP(left, right) =>
        (left(a), right(a)).mapN((_, _) => a)

      case OrP(left, right) =>
        left(a) match {
          case Valid(_)   => Valid(a)
          case Invalid(e1) =>
            right(a) match {
              case Valid(_)   => Valid(a)
              case Invalid(e2) => Invalid(e1 |+| e2)
            }
        }
    }
}

// 为了和上面的object Check进去区分, 后面都加上后缀P
final case class AndP[E, A](
                            left: Predicate[E, A],
                            right: Predicate[E, A]) extends Predicate[E, A]

final case class OrP[E, A](
                           left: Predicate[E, A],
                           right: Predicate[E, A]) extends Predicate[E, A]

final case class PureP[E, A](
                             func: A => Validated[E, A]) extends Predicate[E, A]

// 用Check来表示我们从一个Predicate构建的结构，它也允许对其输入进行转换:
sealed trait CheckT[E, A, B] {
  import CheckT._
  def apply(a: A)(implicit s: Semigroup[E]): Validated[E, B]

  def map[C](func: B => C): CheckT[E, A, C] =
    Map(this, func)

  def flatMap[C](func: B => CheckT[E, A, C]): CheckT[E, A, C] =
    FlatMap(this, func)

  def andThen[C](that: CheckT[E, B, C]): CheckT[E, A, C] =
    AndThen(this, that)
}

object CheckT {
  final case class AndThen[E, A, B, C](check1: CheckT[E, A, B],
                                    check2: CheckT[E, B, C]) extends CheckT[E, A, C] {
    def apply(in: A)(implicit s: Semigroup[E]): Validated[E, C] = {
      check1(in).withEither(_.flatMap(b => check2(b).toEither))
    }
  }

  final case class FlatMap[E, A, B, C](
                                        check: CheckT[E, A, B],
                                        func: B => CheckT[E, A, C]) extends CheckT[E, A, C] {
    def apply(in: A)(implicit s: Semigroup[E]): Validated[E, C] = {
      // 这他妈谁懂啊
      check(in).withEither(_.flatMap(b => func(b)(in).toEither))
    }
  }

  final case class Map[E, A, B, C](
                                    check: CheckT[E, A, B],
                                    func: B => C) extends CheckT[E, A, C] {

    def apply(in: A)(implicit s: Semigroup[E]): Validated[E, C] =
      check(in).map(func)
  }

  final case class Pure[E, A](
                               pred: Predicate[E, A]) extends CheckT[E, A, A] {

    def apply(in: A)(implicit s: Semigroup[E]): Validated[E, A] =
      pred(in)
  }

  def apply[E, A](pred: Predicate[E, A]): CheckT[E, A, A] =
    Pure(pred)
}


