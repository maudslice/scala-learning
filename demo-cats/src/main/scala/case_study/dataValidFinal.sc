// 对在dataValidation代码的最终实现, 包括对其中代码的整理和包装:

import cats.Semigroup
import cats.data.Validated
import cats.data.Validated._ // for Valid and Invalid
import cats.syntax.semigroup._ // for |+|
import cats.syntax.apply._ // for mapN
import cats.syntax.validated._ // for valid and invalid

// 对Predicate的完整实现，包括and和or组合器和一个Predicate.apply方法来从一个函数中创建一个Predicate:
sealed trait Predicate[E, A] {

  import Predicate._
  import Validated._

  def and(that: Predicate[E, A]): Predicate[E, A] =
    And(this, that)

  def or(that: Predicate[E, A]): Predicate[E, A] =
    Or(this, that)

  def apply(a: A)(implicit s: Semigroup[E]): Validated[E, A] =
    this match {
      case Pure(func) =>
        func(a)

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

object Predicate {
  final case class And[E, A](
                              left: Predicate[E, A],
                              right: Predicate[E, A]) extends Predicate[E, A]

  final case class Or[E, A](
                             left: Predicate[E, A],
                             right: Predicate[E, A]) extends Predicate[E, A]

  final case class Pure[E, A](
                               func: A => Validated[E, A]) extends Predicate[E, A]

  def apply[E, A](f: A => Validated[E, A]): Predicate[E, A] =
    Pure(f)

  def lift[E, A](err: E, fn: A => Boolean): Predicate[E, A] =
    Pure(a => if (fn(a)) a.valid else err.invalid)
}

// 下面是Check的完整实现。由于Scala的模式匹配有一个类型推理的错误，我们改用继承的方式实现apply:
sealed trait Check[E, A, B] {

  import Check._

  def apply(in: A)(implicit s: Semigroup[E]): Validated[E, B]

  def map[C](f: B => C): Check[E, A, C] =
    Map[E, A, B, C](this, f)

  def flatMap[C](f: B => Check[E, A, C]): Check[E, A, C] =
    FlatMap[E, A, B, C](this, f)

  def andThen[C](next: Check[E, B, C]): Check[E, A, C] =
    AndThen[E, A, B, C](this, next)
}

object Check {
  final case class Map[E, A, B, C](check: Check[E, A, B], func: B => C) extends Check[E, A, C] {
    def apply(a: A)(implicit s: Semigroup[E]): Validated[E, C] =
      check(a) map func
  }

  final case class FlatMap[E, A, B, C](check: Check[E, A, B], func: B => Check[E, A, C]) extends Check[E, A, C] {
    def apply(a: A)(implicit s: Semigroup[E]): Validated[E, C] =
      check(a).withEither(_.flatMap(b => func(b)(a).toEither))
  }

  final case class AndThen[E, A, B, C](check: Check[E, A, B], next: Check[E, B, C]) extends Check[E, A, C] {
    def apply(a: A)(implicit s: Semigroup[E]): Validated[E, C] =
      check(a).withEither(_.flatMap(b => next(b).toEither))
  }

  final case class Pure[E, A, B](func: A => Validated[E, B]) extends Check[E, A, B] {
    def apply(a: A)(implicit s: Semigroup[E]): Validated[E, B] =
      func(a)
  }

  final case class PurePredicate[E, A](pred: Predicate[E, A]) extends Check[E, A, A] {
    def apply(a: A)(implicit s: Semigroup[E]): Validated[E, A] =
      pred(a)
  }

  def apply[E, A](pred: Predicate[E, A]): Check[E, A, A] =
    PurePredicate(pred)

  def apply[E, A, B]
  (func: A => Validated[E, B]): Check[E, A, B] =
    Pure(func)
}

// 上面的Check和Predicate实现完成了最初设定的大部分工作, 但是, 它还没有最终完成, 你可能已经在Predicate和Check中认识到了我们可以抽象出来的结构：Predicate有一个monoid，Check有一个monoid。此外，在实现Check的过程中，你可能已经感觉到实现并没有做什么--我们所做的只是调用Predicate和Validated上的基础方法。
//
//这个库有很多方法可以被清理掉。然而，让我们实现一些例子来证明我们的库确实有效，然后将再次改进它。

// 下面给出一些需要Check的例子:
// 一个用户名必须至少包含四个字符，并且完全由字母数字字符组成
//一个电子邮件地址必须包含一个@符号。在@处分割字符串。左边的字符串不能是空的。右边的字符串必须至少有三个字符并且包含一个点。

// 下面是提前定义的一些可能会用到的Predicate

import cats.data.{NonEmptyList, Validated}

type Errors = NonEmptyList[String]

def error(s: String): NonEmptyList[String] =
  NonEmptyList(s, Nil)

def longerThan(n: Int): Predicate[Errors, String] =
  Predicate.lift(
    error(s"Must be longer than $n characters"),
    str => str.size > n)

val alphanumeric: Predicate[Errors, String] =
  Predicate.lift(
    error(s"Must be all alphanumeric characters"),
    str => str.forall(_.isLetterOrDigit))

def contains(char: Char): Predicate[Errors, String] =
  Predicate.lift(
    error(s"Must contain the character $char"),
    str => str.contains(char))

def containsOnce(char: Char): Predicate[Errors, String] =
  Predicate.lift(
    error(s"Must contain the character $char only once"),
    str => str.filter(c => c == char).size == 1)

// 一个用户名必须至少包含四个字符，并且完全由字母数字字符组成
val checkUsername: Check[Errors, String, String] =
  Check(longerThan(3) and alphanumeric)

// 一个电子邮件地址必须包含一个@符号。在@处分割字符串。左边的字符串不能是空的。右边的字符串必须至少有三个字符并且包含一个点。
val splitEmail: Check[Errors, String, (String, String)] =
  Check(_.split('@') match {
    case Array(name, domain) =>
      (name, domain).validNel[String]

    case _ =>
      "Must contain a single @ character".
        invalidNel[(String, String)]
  })

val checkLeft: Check[Errors, String, String] =
  Check(longerThan(0))

val checkRight: Check[Errors, String, String] =
  Check(longerThan(3) and contains('.'))

val joinEmail: Check[Errors, (String, String), String] = {
  // 匿名函数格式, 等价于
  // Check((email: (String, String)) => email match {
  //  case (l, r) => (checkLeft(l), checkRight(r)).mapN(_ + "@" + _)
  //})
  Check(_ match {
    case (l, r) =>
      (checkLeft(l), checkRight(r)).mapN(_ + "@" + _)
  })
}


val checkEmail: Check[Errors, String, String] =
  splitEmail andThen joinEmail

// 使用CheckUser和CheckEmail
final case class User(username: String, email: String)

def createUser(username: String, email: String): Validated[Errors, User] =
  (checkUsername(username), checkEmail(email)).mapN(User.apply)

createUser("Noel", "noel@underscore.io")
createUser("", "dave@underscore.io@io")


