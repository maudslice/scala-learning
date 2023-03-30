import cats.implicits._
import cats._

sealed trait Tree[+A]

final case class Branch[A](left: Tree[A], right: Tree[A])
  extends Tree[A]

final case class Leaf[A](value: A) extends Tree[A]

implicit val treeFunctor: Functor[Tree] = new Functor[Tree] {
  override def map[A, B](fa: Tree[A])(f: A => B) = fa match {
    case Leaf(a) => Leaf(f(a))
    case Branch(left, right) => Branch(map(left)(f), map(right)(f))
  }
}

// 使用treeFunctor
//Branch(Leaf(10), Leaf(20)).map(_ * 2)
// error
//value map is not a member of Branch[Int]
//Branch(Leaf(10), Leaf(20)).map(_ * 2)

// 因为Branch[A]和Leaf[A]被声明为不变, 所以无法在其中发现map方法
// 在伴生对象中定义一个智能构造器来解决
object Tree {
  def branch[A](left: Tree[A], right: Tree[A]): Tree[A] = Branch(left, right)

  def leaf[A](value: A): Tree[A] = Leaf(value)
}

// 使用
Tree.branch(Tree.leaf(10), Tree.leaf(20)).map(_ * 2)
Tree.leaf(5).map(_ + 2)

