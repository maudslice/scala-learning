trait Semigroup[A] {
  def combine(x: A, y: A): A
}

trait Monoid[A] extends Semigroup[A] {
  def empty: A
}

object Monoid {
  def apply[A](implicit monoid: Monoid[A]) =
    monoid
}

// 与bool monoid
implicit val boolAndMonoid: Monoid[Boolean] = new Monoid[Boolean] {
  override def empty = true

  override def combine(x: Boolean, y: Boolean) = x && y
}

// 或bool monoid
implicit val boolOrMonoid: Monoid[Boolean] = new Monoid[Boolean] {
  override def empty = false

  override def combine(x: Boolean, y: Boolean) = x || y
}

// 异或bool monoid
implicit val boolEitherMonoid: Monoid[Boolean] = new Monoid[Boolean] {
  override def empty = false

  override def combine(x: Boolean, y: Boolean) = (x && !y) || (!x && y)
}

// 同或bool monoid
implicit val boolXnorMonoid: Monoid[Boolean] = new Monoid[Boolean] {
  override def empty = true

  override def combine(x: Boolean, y: Boolean) = (x || !y) && (!x || y)
}

implicit def setUnionMonoid[A](): Monoid[Set[A]] = new Monoid[Set[A]] {
  override def empty = Set.empty[A]

  override def combine(x: Set[A], y: Set[A]) = x.union(y)
}

val intSetMonoid = Monoid[Set[Int]]
val strSetMonoid = Monoid[Set[String]]

intSetMonoid.combine(Set(1, 2), Set(2, 3))
strSetMonoid.combine(Set("a", "b"), Set("b", "c"))

// 集合相交形成一个Semigroup, 但并不能是一个Monoid, 因为没有identity 元素
implicit def setIntersectionSemigroup[A]: Semigroup[Set[A]] =
  new Semigroup[Set[A]] {
    def combine(x: Set[A], y: Set[A]) =
      x intersect y
  }

// 什么鬼, 这谁懂啊
implicit def symDiffMonoid[A]: Monoid[Set[A]] =
  new Monoid[Set[A]] {
    def combine(a: Set[A], b: Set[A]): Set[A] =
      (a diff b) union (b diff a)
    def empty: Set[A] = Set.empty
  }