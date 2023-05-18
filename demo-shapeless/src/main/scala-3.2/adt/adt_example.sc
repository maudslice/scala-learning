// ADT :代数数据类型（Algebraic data type）
// scala2 ADT 表示:
sealed trait Shape
final case class Rectangle(width: Double, height: Double) extends Shape
final case class Circle(radius: Double) extends Shape

val rect: Shape = Rectangle(3.0, 4.0)
val circ: Shape = Circle(1.0)

def area(shape: Shape): Double =
  shape match {
    case Rectangle(w, h) => w * h
    case Circle(r) => math.Pi * r * r
  }

area(rect)
area(circ)

// scala3 ADT 表示:
enum Shape2:
  case Rectangle2(width: Double, height: Double)
  case Circle2(radius: Double)

val rect2: Shape2 = Shape2.Rectangle2(3.0, 4.0)
val circ2: Shape2 = Shape2.Circle2(1.0)

def area2(shape: Shape2): Double =
  shape match {
    case Shape2.Rectangle2(w, h) => w * h
    case Shape2.Circle2(r) => math.Pi * r * r
  }

area2(rect2)
area2(circ2)


// 使用元组表示ADT
type Rectangle3 = (Double, Double)
type Circle3 = Double
type Shape3 = Either[Rectangle3, Circle3]

val rect3: Shape3 = Left((3.0, 4.0))
val circ3: Shape3 = Right(1.0)

def area3(shape: Shape3): Double =
  shape match {
    case Left((w, h)) => w * h
    case Right(r) => math.Pi * r * r
  }

area3(rect3)
area3(circ3)