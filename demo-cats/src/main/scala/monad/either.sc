val either1: Either[String, Int] = Right(10)
val either2: Either[String, Int] = Right(20)

// in scala 2.11, Either no impl map and flatmap
for {
  a <- either1.right
  b <- either2.right
} yield a + b

// in scala 2.12, Either被重新设计, 支持map和flatmap
for {
  a <- either1
  b <- either2
} yield a + b

// 通过cats, 可以在任何支持的scala版本中使用either的map和flatmap
// scala2.12 以后版本可以不用导入

import cats.syntax.either._

import scala.concurrent.Future // for map and flatMap

for {
  a <- either1
  b <- either2
} yield a + b

// 除了直接使用Left, Right之外, 还可以使用从cats.syntax.either导入的asLeft和asRight方法
val a = 3.asRight[String]
val b = 5.asRight[String]

for {
  ax <- a
  bx <- b
} yield ax + bx

// asRight的一些优势
//def countPositive(nums: List[Int]) =
//  nums.foldLeft(Right(0)) { (accumulator, num) =>
//    if(num > 0) {
//      accumulator.map(_ + 1)
//    } else {
//      Left("Negative. Stopping!")
//    }
//  }

// 上面的例子无法编译, 因为Right.apply返回的时候Right类型, 所以要求返回Right类型
// 使用Right时省略了Right的类型参数, 导致编译器推断Left的类型为Nothing

// asRight和asLeft方法返回的是Either类型, 可以避免上面的问题, 同时也运行只使用一个类型参数就来声明Either的Left和Right的类型
def countPositive(nums: List[Int]) =
  nums.foldLeft(0.asRight[String]) { (accumulator, num) =>
    if (num > 0) {
      accumulator.map(_ + 1)
    } else {
      Left("Negative. Stopping!")
    }
  }

countPositive(List(1, 2, 3))
countPositive(List(1, -2, 3))

// cats.syntax.either为Either的伴生对象提供了一些有用的方法
// 捕获exception
Either.catchOnly[NumberFormatException]("foo".toInt)
Either.catchNonFatal(sys.error("Badness"))

// 从其他类型创建一个Either
Either.fromTry(scala.util.Try("foo".toInt))
Either.fromOption[String, Int](None, "Badness")

// 为Either实例提供的一些有用的方法
// 使用orElse和getOrElse提取Right的值
"Error".asLeft[Int].getOrElse(0)
"Error".asLeft[Int].orElse(2.asRight[String])

// 使用ensure方法检查Right值是否满足某个前提
(-1).asRight[String].ensure("Must be non-negative!")(_ > 0)

// 使用recover和recoverWith像Future一样处理错误
"error".asLeft[Int].recover {
  case _ => -1
}

"error".asLeft[Int].recoverWith {
  case _: String => Right(-1)
}

// 使用leftMap和bimap来补充map
// 对Left值应用map上的func
"foo".asLeft[Int].leftMap(_.reverse)
// 分别对Left值和Right值应用func1和func2
6.asRight[String].bimap(_.reverse, _ * 7)
"bar".asLeft[Int].bimap(_.reverse, _ * 7)

// 使用swap交换Left和Right值
123.asRight[String]
123.asRight[String].swap

// cats还增加了一些转换方法, 如toOption toList toTry toValidated等
