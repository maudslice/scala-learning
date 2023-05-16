trait Printable[A] { self =>
  def format(value: A): String

  // contramap functor: 将一个操作预置到一条链上, 它只对表示转换的数据类型有意义
  def contramap[B](func: B => A): Printable[B] =
    new Printable[B] {
      def format(value: B): String =
        self.format(func(value))
    }
}

def format[A](value: A)(implicit p: Printable[A]): String =
  p.format(value)

// test this
implicit val stringPrintable: Printable[String] =
  new Printable[String] {
    def format(value: String): String =
      s"'$value'"
  }

implicit val booleanPrintable: Printable[Boolean] =
  new Printable[Boolean] {
    def format(value: Boolean): String =
      if(value) "yes" else "no"
  }

format("kaka!")
format(true )

final case class Box[A](value: A)

// 使用contramap从现有实例中构建Box的Printable实例
implicit def boxPrintable[A](implicit printable: Printable[A]): Printable[Box[A]] =
  printable.contramap[Box[A]](_.value)

format(Box("hello world"))
format(Box(false))