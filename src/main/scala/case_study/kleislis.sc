// 在dataValidFinal中, 我们对Check和Predicate做了完整的实现
// 对它们的一个合理的批评是, 我们用了很多的代码来做很少的事情
// Predicate是实质是一个函数A => Validated[E, A], Check本质上是一个包装器, 让我们能组合这些函数
// 实际上, 我们可以将A => Validated[E, A]抽象为A => F[B], 这个Monad中传递给flatmap的函数类型一样
// 考虑如下操作:
// 将一个值提升(lift)到一个monad中(比如使用pure), 这是一个A => F[A]的函数, 然后使用flatmap对monad进行一些转换序列, 就像下面的代码:
/*
val aToB: A => F[B] = ???
val bToC: B => F[C] = ???

def example[A, C](a: A): F[C] =
  aToB(a).flatMap(bToC)
 */
// 上面的代码可以使用andThen写成: val aToC = aToB andThen bToC
// aToC的类型是A => F[C]
// 我们实现了与示例方法相同的事情，而不必引用A类型的参数。Check上的andThen方法类似于函数组合，但组合的是函数A => F[B]而不是A => B。


// 组合A=>F[B]类型的函数的抽象概念有一个名字：Kleisli。
// Cats包含一个数据类型cats.data.Kleisli，它就像Check一样包装了一个函数。
// 实际上, Kleisli只是ReaderT的另一个名字

// 下面是一些使用Kleisli的简单例子, 通过三个步骤把一个整数转换为一个整数列表

import cats.Semigroup
import cats.data.{Kleisli, Validated}
import cats.instances.list._ // for Monad
import cats.syntax.apply._ // for MapN
import cats.syntax.semigroup._ // for |+|
import cats.syntax.validated._ // for valid and invalid
import cats.syntax.parallel._ // for parMapN

// 这些步骤分别将一个Int转换为一个List[Int]类型的输出
val step1: Kleisli[List, Int, Int] =
  Kleisli(x => List(x + 1, x - 1))

val step2: Kleisli[List, Int, Int] =
  Kleisli(x => List(x, -x))

val step3: Kleisli[List, Int, Int] =
  Kleisli(x => List(x * 2, x / 2))

// 可以把这些步骤合并成一个管道，用flatMap把底层的Lists组合起来：
val pipeline = step1 andThen step2 andThen step3

pipeline.run(20)

// Kleisli和Check在API方面的唯一显著区别是，Kleisli将Check的apply方法重命名为run
// 用Kleisli替代Check, 要做到这一点，需要对Predicate做一些改变。必须能够将Predicate转换成一个函数，因为Kleisli只对函数起作用。更微妙的是，当将Predicate转换为函数时，它的类型应该是A => Either[E, A]，而不是A => Validated[E, A]，因为Kleisli依赖于包裹的函数返回一个单体。
//为Predicate添加一个名为run的方法，该方法返回一个正确类型的函数。让Predicate中的其他代码保持不变。

sealed trait Predicate[E, A] {

  import Predicate._
  import Validated._

  def run(implicit s: Semigroup[E]): A => Either[E, A] =
    (a: A) => this(a).toEither

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

// 使用下面的方法重写验证邮件和姓名的例子
import cats.data.NonEmptyList

type Errors = NonEmptyList[String]
type Result[A] = Either[Errors, A]
type Check[A, B] = Kleisli[Result, A, B]

// Create a check from a function:
def check[A, B](func: A => Result[B]): Check[A, B] =
  Kleisli(func)

// Create a check from a Predicate:
def checkPred[A](pred: Predicate[Errors, A]): Check[A, A] =
  Kleisli[Result, A, A](pred.run)

def error(s: String): NonEmptyList[String] =
  NonEmptyList(s, Nil)

def longerThan(n: Int): Predicate[Errors, String] =
  Predicate.lift(
    error(s"Must be longer than $n characters"),
    str => str.length > n)

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
    str => str.count(c => c == char) == 1)

// 一个用户名必须至少包含四个字符，并且完全由字母数字字符组成
val checkUsername: Check[String, String] =
  checkPred(longerThan(3) and alphanumeric)

// 一个电子邮件地址必须包含一个@符号。在@处分割字符串。左边的字符串不能是空的。右边的字符串必须至少有三个字符并且包含一个点。
val splitEmail: Check[String, (String, String)] =
  check(_.split('@') match {
    case Array(name, domain) => Right((name, domain))
    case _ => Left(error("Must contain a single @ character"))
  })

val checkLeft: Check[String, String] =
  checkPred(longerThan(0))

val checkRight: Check[String, String] =
  checkPred(longerThan(3) and contains('.'))

val joinEmail: Check[(String, String), String] = {
  check {
    case (l, r) =>
      (checkLeft(l), checkRight(r)).mapN(_ + "@" + _)
  }
}


val checkEmail: Check[String, String] =
  splitEmail andThen joinEmail

// 使用CheckUser和CheckEmail
final case class User(username: String, email: String)

def createUser(username: String, email: String): Either[Errors, User] =
  (checkUsername.run(username), checkEmail.run(email)).parMapN(User)

createUser("Noel", "noel@underscore.io")
createUser("", "dave@underscore.io@io")


