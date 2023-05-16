import cats.Monoid
import cats.instances.string._
import cats.syntax.invariant._
import cats.syntax.semigroup._ // for |+|

// compiler error ??
implicit val symbolMonoid: Monoid[Symbol] =
  Monoid[String].imap(Symbol.apply)(_.name)

Monoid[Symbol].empty

Symbol("a") |+| Symbol("few") |+| Symbol("words")