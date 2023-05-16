List(1, 2, 3).foldLeft(List.empty[Int])((a, i) => i :: a)
// res6: List[Int] = List(3, 2, 1)
// 使用::作为fold的op, foldLeft得到相反List

List(1, 2, 3).foldRight(List.empty[Int])((i, a) => i :: a)
// res7: List[Int] = List(1, 2, 3)
// foldRight折叠后的顺序保持不变

// scala是基于程序流来进行类型推断, 如果不显式的给出acc的类型, 会发生编译错误
//List(1, 2, 3).foldRight(Nil)(_ :: _)
// error: type mismatch;
//  found   : List[Int]
//  required: scala.collection.immutable.Nil.type

//foldRight实现List的map、flatMap、filter和sum
def map[A, B](as: List[A])(f: A => B): List[B] =
  as.foldRight(List.empty[B])((h, acc) => f(h) :: acc)

map(List(1, 2, 3))(_ * 2)
// res9: List[Int] = List(2, 4, 6)

def flatMap[A, B](as: List[A])(f: A => List[B]): List[B] =
  as.foldRight(List.empty[B])((h, acc) => f(h) ::: acc)

flatMap(List(1, 2, 3))(a => List(a, a * 10, a * 100))
// res10: List[Int] = List(1, 10, 100, 2, 20, 200, 3, 30, 300)

def filter[A](as: List[A])(f: A => Boolean): List[A] =
  as.foldRight(List.empty[A])((h, acc) => if (f(h)) h :: acc else acc)

filter(List(1, 2, 3))(_ % 2 == 1)
// res11: List[Int] = List(1, 3)

def sumViaNumeric[A: Numeric](as: List[A]): A =
  as.foldLeft(summon.zero)(summon.plus)

sumViaNumeric(List(1, 2, 3))

import cats.Monoid
def sumViaMonad[A: Monoid](as: List[A]): A =
  as.foldLeft(summon.empty)(summon.combine)

sumViaMonad(List(1, 2, 3))