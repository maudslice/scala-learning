import cats.Contravariant
import cats.Show
import cats.instances.string._

val showString = Show[String]

val showSymbol = Contravariant[Show].contramap(showString)((sym: Symbol) => s"'${sym.name}")

showSymbol.show(Symbol("dave"))

import cats.syntax.contravariant._ // for contramap
showString
  .contramap[Symbol](sym => s"'${sym.name}")
  .show(Symbol("dave"))