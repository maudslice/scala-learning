//Cats的Foldable将foldLeft和foldRight抽象为一个typeclass。Foldable的实例定义了这两个方法并继承了大量的派生方法。Cats为少量的Scala数据类型提供了开箱即用的Foldable实例：List、Vector、LazyList和Option。

// 通过Foldable的apply方法创建一个它的实例
import cats.Foldable
import cats.instances.list._ // for Foldable

val ints = List(1, 2, 3)

Foldable[List].foldLeft(ints, 0)(_ + _)
// res0: Int = 6

// 其他序列类型也同样如此
import cats.instances.option._ // for Foldable

val maybeInt = Option(123)

Foldable[Option].foldLeft(maybeInt, 10)(_ * _)
// res1: Int = 1230

// 对于Monad Eval, 它的foldRight实现和foldLeft实现稍有不同, 它是堆栈安全的

import cats.Eval

def bigData: LazyList[Int] = (1 to 100000).to(LazyList)

// LazyList默认的foldRight实现不是堆栈安全的
//bigData.foldRight(0L)(_ + _)
// java.lang.StackOverflowError ...

import cats.instances.lazyList._ // for Foldable

val eval: Eval[Long] =
  Foldable[LazyList].
    foldRight(bigData, Eval.now(0L)) { (num, eval) =>
      eval.map(_ + num)
    }

eval.value
// res3: Long = 5000050000L

// 关于标准库中的堆栈安全, 常用的集合类型都是堆栈安全的, 比如List和Vector, 都实现了堆栈安全的foldRight
(1 to 100000).toList.foldRight(0L)(_ + _)
// res4: Long = 5000050000L
(1 to 100000).toVector.foldRight(0L)(_ + _)
// res5: Long = 5000050000L

//Foldable 为我们提供了大量定义在 foldLeft 上的有用方法。其中许多是标准库中熟悉的方法：find, exists, forall, toList, isEmpty, nonEmpty，等等：
Foldable[Option].nonEmpty(Option(42))
// res6: Boolean = true

Foldable[List].find(List(1, 2, 3))(_ % 2 == 0)
// res7: Option[Int] = Some(2)

// combineAll（以及它的别名fold）使用它们的Monoid来组合序列中的所有元素
import cats.instances.int._ // for Monoid

Foldable[List].combineAll(List(1, 2, 3))
// res8: Int = 6

// flatMap
import cats.instances.string._ // for Monoid

Foldable[List].foldMap(List(1, 2, 3))(_.toString)
// res9: String = "123"

// 组合Foldables来支持嵌套序列的深度遍历
import cats.instances.vector._ // for Monoid

val ints = List(Vector(1, 2, 3), Vector(4, 5, 6))

(Foldable[List] compose Foldable[Vector]).combineAll(ints)
// res11: Int = 21

// 通过foldable的语法来使用foldable
// Foldable上的方法的第一个参数都会成为方法调用的接收方
import cats.syntax.foldable._ // for combineAll and foldMap

List(1, 2, 3).combineAll
// res12: Int = 6

List(1, 2, 3).foldMap(_.toString)
// res13: String = "123"