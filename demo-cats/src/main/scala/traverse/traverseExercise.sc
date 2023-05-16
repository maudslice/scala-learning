import cats.Applicative
import cats.syntax.apply._ // for mapN
import cats.syntax.applicative._ // for pure
import cats.instances.vector._ // for Applicative


def listTraverse[F[_]: Applicative, A, B]
(list: List[A])(func: A => F[B]): F[List[B]] =
  list.foldLeft(List.empty[B].pure[F]) { (accum, item) =>
    (accum, func(item)).mapN(_ :+ _)
  }

def listSequence[F[_]: Applicative, B]
(list: List[F[B]]): F[List[B]] =
  listTraverse(list)(identity)

// Traversing with Vectors
listSequence(List(Vector(1, 2), Vector(3, 4)))
// 2 * 2 = 4
// res0: Vector(List(1, 3), List(1, 4), List(2, 3), List(2, 4))

listSequence(List(Vector(1, 2), Vector(3, 4), Vector(5, 6)))
// 2 * 2 * 2 = 8
// res1: Vector(List(1, 3, 5), List(1, 3, 6), List(1, 4, 5), List(1, 4, 6), List(2, 3, 5), List(2, 3, 6), List(2, 4, 5), List(2, 4, 6))

// Traversing with Options
import cats.instances.option._ // for Applicative

def process(inputs: List[Int]): Option[List[Int]] =
  listTraverse(inputs)(n => if(n % 2 == 0) Some(n) else None)

process(List(2, 4, 6))
// res2: Some(List(2, 4, 6))
process(List(1, 2, 3))
// res3: None

// Traversing with Validated
import cats.data.Validated
import cats.instances.list._ // for Monoid

type ErrorsOr[A] = Validated[List[String], A]

def process2(inputs: List[Int]): ErrorsOr[List[Int]] =
  listTraverse(inputs) { n =>
    if(n % 2 == 0) {
      Validated.valid(n)
    } else {
      Validated.invalid(List(s"$n is not even"))
    }
  }

process2(List(2, 4, 6))
// res4: Valid(List(2, 4, 6))
process2(List(1, 2, 3))
// res5: Invalid(List(1 is not even, 3 is not even))
// 在validated上的semigroupal combine的语义是累积错误处理，所以结果要么是一个偶数Ints的列表，要么是一个错误列表