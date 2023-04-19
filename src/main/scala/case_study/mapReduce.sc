// 在这个案例中, 我们将会使用Monoid, Functors和一起一系列的好多西来实现一个简单而强大的并行处理框架MapReduce, Map阶段对应Scala Functor中的map函数, Reduce阶段在scala中通常称为fold
// map: 将一个A => B的函数应用在F[A]上, 返回一个F[B]
// map独立的对序列中的每个元素进行转换, 元素之间没有依赖关系, 所以可以很容易的将map并行化(假设我们不使用没有反映在类型中的副作用)

// fold: F[A] foldLeft (B)((B, A) => B) 返回F[B]

// 在分布式的reduce阶段, 我们会失去对遍历顺序的控制, 所以整体的reduce过程可能不完全是从左到右的, 可能需要在几个子序列中从左到右的进行reduce, 再将最后的结果合并
// 所以需要一个对每个阶段的结果进行合并的关联操作:
// reduce(a1, reduce(a2, a3)) == reduce(reduce(a1, a2), a3)

// 如果我们有关联操作，只要每个节点的子序列与初始数据集保持相同的顺序，就可以在节点之间任意分配工作。
// 折叠操作要求用一个B类型的元素作为计算的种子。由于折叠可以被分成任意数量的并行步骤，种子不应该影响计算的结果。这自然要求种子是一个identity element:
// reduce(seed, a1) == reduce(a1, seed) == a1

// 综上所述, 如果我们的并行fold想要得到正确的结果, 需要满足以下两个条件:
// 1. reduce函数是具有关联性的(可以合并结果)
// 2. 用这个函数的identity作为计算的seed

// 首先写出foldMap的类型签名:

import cats.Monoid
import cats.syntax.semigroup._ // for |+|

/** Single-threaded map-reduce function.
 * Maps `func` over `values` and reduces using a `Monoid[B]`.
 */
def foldMap[A, B: Monoid](value: Vector[A])(f: A => B): B = {
  // 按照以下步骤实现foldMap的方法体:
  // 1. 从一个A类型的序列开始
  // 2. 将f应用到value上, 得到value[B]
  // 3. 使用Monoid合并value[B], 得到一个单一的B
  //  value.map(f).foldLeft(Monoid.empty[B])(_ |+| _)

  // 或者直接一步到位
  value.foldLeft(Monoid.empty[B])(_ |+| f(_))
}

// 现在有了一个单线程的foldMap, 下面考虑如何写一个多cpu的实现, 模拟map reduce的工作方式
// 1. 从一个需要处理的所有数据的初始列表开始
// 2. 把数据分成几批, 向每个cpu发送一批
// 3. 每个cpu并行运行一个map阶段
// 4. 每个cpu并行运行一个reduce阶段, 为每批数据产生一个本地结果
// 5. 对每批数据的结果执行reduce, 直到最后得到一个单一的最终结果

// Scala提供了一些简单的工具来在线程之间分配工作。可以使用并行集合库来实现一个解决方案，但让我们挑战一下自己，深入一点，用Futures自己实现这个算法。

// 使用java api查询机器上可用cpu的数量:
Runtime.getRuntime.availableProcessors()

// 使用grouped方法对一个序列进行分割, 使用这个方法来为每个cpu分割出一个任务
(1 to 10).toList.grouped(3).toList
// res12: List[List[Int]] = List(
//   List(1, 2, 3),
//   List(4, 5, 6),
//   List(7, 8, 9),
//   List(10)
// )

// 实现 foldMap 的并行版本parallelFoldMap:

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

def parallelFoldMap[A, B: Monoid](values: Vector[A])(func: A => B): Future[B] = {
  // Calculate the number of items to pass to each CPU:
  val numCores = Runtime.getRuntime.availableProcessors
  val groupSize = (1.0 * values.size / numCores).ceil.toInt

  val futures = values.grouped(groupSize) // 为每个cpu创建一个group
    .map(group => Future(group.foldLeft(Monoid.empty[B])(_ |+| func(_)))) // 每个foldMap创建一个Future
  Future.sequence(futures)
    .map(i => i.foldLeft(Monoid.empty[B])(_ |+| _))
}

// 使用
val result: Future[Int] = parallelFoldMap((1 to 1000000).toVector)(identity)

Await.result(result, 1.second)
// res14: Int = 1784293664

// 使用使用Cats的 typeclass Foldable和Traverseable重新实现parallelFoldMap。

import cats.instances.int._    // for Monoid
import cats.instances.future._ // for Applicative and Monad
import cats.instances.vector._ // for Foldable and Traverse

import cats.syntax.foldable._  // for combineAll and foldMap
import cats.syntax.traverse._  // for traverse

def parallelFoldMap[A, B: Monoid](values: Vector[A])(func: A => B): Future[B] = {
  // Calculate the number of items to pass to each CPU:
  val numCores = Runtime.getRuntime.availableProcessors
  val groupSize = (1.0 * values.size / numCores).ceil.toInt

  values.grouped(groupSize)
    .toVector
    .traverse(group => Future(group.foldMap(func)))
    .map(_.combineAll)
}