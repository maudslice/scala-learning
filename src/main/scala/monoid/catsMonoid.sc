import cats.Monoid
import cats.Semigroup

Monoid[String].combine("hi", " there")
Monoid[String].empty

// 如果不需要empty, 可以等价的编写semigroup
Semigroup[String].combine("hi", " there")

// 导入cats的Monoid[Int]实例
import cats.instances.int._
Monoid[Int].combine(2, 5)

// 导入cats的Monoid[Option]实例和Int混合使用
import cats.instances.option._
val a = Option(1)
val b = Option(4)
Monoid[Option[Int]].combine(a, b)

// 除非我们有充分的理由只导入一个实例, 否则, 通常导入所有的实例
import cats._
import cats.implicits._

// 使用cats提供的Monoid方法运算符
import cats.syntax.semigroup._
"hi" |+| " there" |+| Monoid[String].empty