// ADT :代数数据类型（Algebraic data type）
// scala2 ADT 表示:
sealed trait Shape
final case class Rectangle(width: Double, height: Double) extends Shape
final case class Circle(radius: Double) extends Shape

// scala3 ADT 表示: