import cats.implicits._
import cats._

Semigroup[Int].combine(1, 2)
Semigroup[List[Int]].combine(List(1, 2, 3), List(4, 5, 6))
Semigroup[Option[Int]].combine(Option(1), Option(2))
Semigroup[Option[Int]].combine(Option(1), None)


Monad[Option].ifM(Option(true))(Option("truthy"), Option("falsy"))
Monad[List].ifM(List(true, false, true))(List(1, 2), List(3, 4))

Foldable[List].foldK(List(None, Option("two"), Option("three")))
Foldable[List].foldK(List(List(1, 2), List(3, 4, 5)))

import cats.data.{Validated, ValidatedNel}

def parseIntEither(s: String): Either[NumberFormatException, Int] =
  Either.catchOnly[NumberFormatException](s.toInt)

def parseIntValidated(s: String): ValidatedNel[NumberFormatException, Int] =
  Validated.catchOnly[NumberFormatException](s.toInt).toValidatedNel

List("1", "2", "3").traverse(parseIntEither)
List("1", "abc", "3").traverse(parseIntEither).isLeft

object EitherStyle {
  def parse(s: String): Either[NumberFormatException, Int] =
    if (s.matches("-?[0-9]+")) Either.right(s.toInt)
    else Either.left(new NumberFormatException(s"${s} is not a valid integer."))

  def reciprocal(i: Int): Either[IllegalArgumentException, Double] =
    if (i == 0) Either.left(new IllegalArgumentException("Cannot take reciprocal of 0."))
    else Either.right(1.0 / i)

  def stringify(d: Double): String = d.toString

  def magic(s: String): Either[Exception, String] =
    parse(s).flatMap(reciprocal).map(stringify)
}

import EitherStyle._

val result = magic("2") match {
  case Left(_: NumberFormatException) => "Not a number!"
  case Left(_: IllegalArgumentException) => "Can't take reciprocal of 0!"
  case Left(_) => "Unknown error"
  case Right(result) => s"Got reciprocal: ${result}"
}