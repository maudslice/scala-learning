import cats.implicits._
import cats._

// SuperAdder

// normal impl
def add(items: List[Int]): Int = items.foldLeft(0)(_ + _)
// use monoid impl
def add(items: List[Int]): Int = items.foldLeft(Monoid[Int].empty)(_ |+| _)

def add[A](item: List[A])(implicit monoid: Monoid[A]): A =
  item.foldLeft(Monoid[A].empty)(_ |+| _)

// use bound context
def add[A: Monoid](item: List[A]): A =
  item.foldLeft(implicitly[Monoid[A]].empty)(_ |+| _)

add(List(1, 2, 3, 4))
add(List(Option(1), Option(2), None, None))

// use the add
case class Order(totalCost: Double, quantity: Double)

implicit val orderMonoid: Monoid[Order] = new Monoid[Order] {
  override def empty = Order(0, 0)

  override def combine(x: Order, y: Order) = Order(x.totalCost + y.totalCost, x.quantity + y.quantity)
}