trait Codec[A] { self =>
  def encode(value: A): String
  def decode(value: String): A

  // imap方法某种程度上来说等于map + contramap
  def imap[B](dec: A => B, enc: B => A): Codec[B] = new Codec[B] {
    override def encode(value: B) = self.encode(enc(value))

    override def decode(value: String) = dec(self.decode(value))
  }
}

def encode[A](value: A)(implicit c: Codec[A]): String =
  c.encode(value)

def decode[A](value: String)(implicit c: Codec[A]): A =
  c.decode(value)

implicit val stringCodec: Codec[String] =
  new Codec[String] {
    def encode(value: String): String = value
    def decode(value: String): String = value
  }

// 使用实例, 通过stringCodec来构建其他的Codec
implicit val intCodec: Codec[Int] = stringCodec.imap[Int](_.toInt, _.toString)
implicit val boolCodec: Codec[Boolean] = stringCodec.imap[Boolean](_.toBoolean, _.toString)
implicit val doubleCodec: Codec[Double] = stringCodec.imap[Double](_.toDouble, _.toString)

final case class Box[A](value: A)
implicit def boxCodec[A](implicit c: Codec[A]): Codec[Box[A]] = c.imap[Box[A]](Box(_), _.value)

encode(123)
encode(true)
decode[Int]("123")
decode[Boolean]("true")
encode(Box("kaka"))
decode[Box[String]]("wdc")