import shapeless3.deriving.*

// 如何利用类型之间的相似性来避免重复?
case class Employee(name: String, number: Int, manager: Boolean)

case class IceCream(name: String, numCherries: Int, inCone: Boolean)

// 这两个案例类代表了不同种类的数据，但它们有明显的相似之处：它们都包含相同类型的三个字段。假设我们想实现一个通用的操作，如序列化到CSV文件。尽管这两种类型有相似之处，但我们还是要写两个独立的序列化方法：
def employeeCsv(e: Employee): List[String] =
  List(e.name, e.number.toString, e.manager.toString)

def iceCreamCsv(c: IceCream): List[String] =
  List(c.name, c.numCherries.toString, c.inCone.toString)

// 泛型编程就是要克服像这样的差异。Shapeless使我们能够方便地将特定的类型转换成我们可以用普通代码操作的通用类型。

val genericEmployee: String *: Int *: Boolean *: EmptyTuple = Tuple.fromProductTyped(Employee("Dave", 123, false))

val genericIceCream: String *: Int *: Boolean *: EmptyTuple = Tuple.fromProductTyped(IceCream("Sundae", 1, false))

// 现在两个值都是同一类型, 它们都是heterogeneous lists(简称HList), 下面使用同一个序列化函数来序列化这两个值
def genericCsv(gen: String *: Int *: Boolean *: EmptyTuple): List[String] = {
  // 似乎是idea scala plugin的bug, scalac可以正常编译下面的代码
  // 码的怎么这么多bug
  List(gen(0), gen(1).toString, gen(2).toString)
}

genericCsv(genericEmployee)
// res2: List[String] = List(Dave, 123, false)

genericCsv(genericIceCream)
// res3: List[String] = List(Sundae, 1, false)