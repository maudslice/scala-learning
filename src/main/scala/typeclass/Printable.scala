package typeclass

import typeclass.Main.Cat

/**
 * @date 2023-03-22 14:04
 * @author chenzhr
 * @Description
 */
trait Printable[A] {
  def format(value: A): String
}

object Printable {
  implicit val stringPrinter: Printable[String] = value => value
  implicit val intPrinter: Printable[Int] = value => value.toString
  implicit val catPrinter: Printable[Cat] = cat => s"${cat.name} is a ${cat.age} year-old ${cat.color} cat."
}
