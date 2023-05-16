import cats.data.Writer
import cats.instances.vector._ // for Monoid

// Write是一个可以在计算的同时携带日志的Monad, 可以用来记录信息, 错误, 或关于计算的额外数据, 并在最终结果旁边提取日志
// Writer[W, A]有两个类型参数, W是日志的类型, A是结果类型
Writer(Vector(
  "It was the best of times",
  "it was the worst of times"
), 1859)

// 实际类型是WriterT[cats.Id,scala.collection.immutable.Vector[String],Int]
// Writer是WriterT的一个别名

// 使用pure从一个值中获得Writer实例
import cats.syntax.applicative._ // for pure

type Logged[A] = Writer[Vector[String], A]
123.pure[Logged]
// ? 作为 Writer 的类型参数 A 的占位符。这个占位符告诉编译器我们想要使用 Writer[Vector[String], A] 的部分应用类型
//123.pure[Writer[Vector[String], ?]]

// 创建Writer实例:
// 如果只有日志没有结果, 可以使用cats.syntax.writer提供的tell语法创建一个Writer[Unit]
import cats.syntax.writer._ // for tell

Vector("msg1", "msg2", "msg3").tell

// 同时有结果或者日志
// 使用Writer.apply
val a = Writer(Vector("msg1", "msg2", "msg3"), 123)
// 使用cats.syntax.writer提供的语法
val b = 123.writer(Vector("msg1", "msg2", "msg3"))

// 使用value和written方法分别从Writer中提取结果和日志
val aRes: Int = a.value
val aLog: Vector[String] = a.written

// 使用run方法同时提取两个值
val (res, log) = a.run

// 对Writer使用map和flatmap时, 其中的日志会被保留下来, flatmap会累积source writer上的日志和计算函数, 出于这个原因, 使用具有高效追加和连接操作的日志类型是一个比较好的做法, 比如Vector
val writer1 = for {
  a <- 10.pure[Logged]
  _ <- Vector("a", "b", "c").tell
  b <- 32.writer(Vector("x", "y", "z"))
} yield a + b

writer1.run
//val writer1: cats.data.WriterT[cats.Id,Vector[String],Int] = WriterT((Vector(a, b, c, x, y, z),42))

// 使用mapWritten方法转换Writer中的日志
val writer2 = writer1.mapWritten(_.map(_.toUpperCase))
writer2.run
//val res4: cats.Id[(scala.collection.immutable.Vector[String], Int)] = (Vector(A, B, C, X, Y, Z),42)

// 使用bimap和mapBoth同时转换日志和结果
val writer3 = writer1.bimap(
  log => log.map(_.toUpperCase),
  res => res * 100
)
// writer3: cats.data.WriterT[cats.package.Id, Vector[String], Int] = WriterT(
//   (Vector("A", "B", "C", "X", "Y", "Z"), 4200)
// )

writer3.run
// res5: (Vector[String], Int) = (Vector("A", "B", "C", "X", "Y", "Z"), 4200)

val writer4 = writer1.mapBoth { (log, res) =>
  val log2 = log.map(_ + "!")
  val res2 = res * 1000
  (log2, res2)
}
// writer4: cats.data.WriterT[cats.package.Id, Vector[String], Int] = WriterT(
//   (Vector("a!", "b!", "c!", "x!", "y!", "z!"), 42000)
// )

writer4.run
// res6: (Vector[String], Int) = (
//   Vector("a!", "b!", "c!", "x!", "y!", "z!"),
//   42000
// )

// 使用reset方法清空日志
val writer5 = writer1.reset
// writer5: cats.data.WriterT[cats.package.Id, Vector[String], Int] = WriterT(
//   (Vector(), 42)
// )

writer5.run
// res7: (Vector[String], Int) = (Vector(), 42)

// 使用swap方法交换日志和结果
val writer6 = writer1.swap
// writer6: cats.data.WriterT[cats.package.Id, Int, Vector[String]] = WriterT(
//   (42, Vector("a", "b", "c", "x", "y", "z"))
// )

writer6.run
// res8: (Int, Vector[String]) = (42, Vector("a", "b", "c", "x", "y", "z"))