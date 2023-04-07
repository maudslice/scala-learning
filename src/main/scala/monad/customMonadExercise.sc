sealed trait Tree[+A]

final case class Branch[A](left: Tree[A], right: Tree[A])
  extends Tree[A]

final case class Leaf[A](value: A) extends Tree[A]

def branch[A](left: Tree[A], right: Tree[A]): Tree[A] =
  Branch(left, right)

def leaf[A](value: A): Tree[A] =
  Leaf(value)

import cats.Monad

import scala.annotation.tailrec

implicit val treeMonad: Monad[Tree] = new Monad[Tree] {
  override def flatMap[A, B](fa: Tree[A])(f: A => Tree[B]): Tree[B] =
    fa match {
      case Branch(left, right) => branch(flatMap(left)(f), flatMap(right)(f))
      case Leaf(value) => f(value)
    }

  // 非堆栈安全的实现
  //  override def tailRecM[A, B](a: A)(f: A => Tree[Either[A, B]]) = flatMap(f(a)){
  //    case Left(value) => tailRecM(value)(f)
  //    case Right(value) => Leaf(value)
  //  }

  // 堆栈安全的实现
  override def tailRecM[A, B](arg: A)(func: A => Tree[Either[A, B]]): Tree[B] = {
    @tailrec
    def loop(
              open: List[Tree[Either[A, B]]],
              closed: List[Option[Tree[B]]]): List[Tree[B]] =
      open match {
        case Branch(l, r) :: next =>
          loop(l :: r :: next, None :: closed)

        case Leaf(Left(value)) :: next =>
          loop(func(value) :: next, closed)

        case Leaf(Right(value)) :: next =>
          loop(next, Some(pure(value)) :: closed)

        case Nil =>
          closed.foldLeft(Nil: List[Tree[B]]) { (acc, maybeTree) =>
            maybeTree.map(_ :: acc).getOrElse {
              val left :: right :: tail = acc
              branch(left, right) :: tail
            }
          }
      }

    loop(List(func(arg)), Nil).head
  }

  override def pure[A](x: A): Tree[A] = leaf(x)
}

// 无论使用哪个版本的tailRecM, 都可以对Tree使用map和flatmap
import cats.syntax.functor._ // for map
import cats.syntax.flatMap._ // for flatMap

branch(leaf(100), leaf(200)).
  flatMap(x => branch(leaf(x - 1), leaf(x + 1)))

for {
  a <- branch(leaf(100), leaf(200))
  b <- branch(leaf(a - 10), leaf(a + 10))
  c <- branch(leaf(b - 1), leaf(b + 1))
} yield c

// Option的单体提供了故障快速的语义。List的单体提供了连接的语义。
// flatMap对二叉树的语义是什么？树中的每个节点都有可能被整个子树取代，产生一种 "生长 "或 "羽化 "行为，让人联想到沿两个轴的列表连接。