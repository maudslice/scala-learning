package typeclass

/**
 * @date 2023-03-22 14:08
 * @author chenzhr
 * @Description
 */
object PrintableSyntax {
  implicit class PrintableOps[A](input: A) {
    def format(implicit p: Printable[A]): String = p.format(input)
    def print(implicit p: Printable[A]): Unit = println(format(p))
  }

}
