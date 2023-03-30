// 通过cats的Eq typeclass来实现类型安全的比较
import cats.Eq
import cats.instances.int._
val eqInt = Eq[Int]

eqInt.eqv(123, 123)
// res1: Boolean = true
eqInt.eqv(123, 234)
// res2: Boolean = false

//eqInt.eqv(123, "234")
// error: type mismatch;
//  found   : String("234")
//  required: Int

import cats.syntax.eq._ //使用 === 和 =!=
123 === 123
123 =!= 321

// 同样, 比较不同类型会导致编译错误
//123 === 123.0
// error: type mismatch

// 比较option
import cats.instances.option._
//Some(1) === None //编译错误
// error: value === is not a member of Some[Int]
// Some(1) === None
// ^^^^^^^^^^^

(Some(1) : Option[Int]) === (None : Option[Int])
// 更好的方式
Option(1) === Option.empty[Int]
// 使用cats的特殊语法
import cats.syntax.option._ // for some and none
1.some === none[Int]
1.some =!= none[Int]

//自定义Eq的typeclass实例
import java.util.Date
import cats.instances.long._

implicit val dateEq: Eq[Date] = Eq.instance((d1, d2) => d1.getTime === d2.getTime)
val x = new Date()
val y = new Date() // 晚一点的时间

x === y
x === y

// Exercise: Equality, Liberty, and Felinity
// Implement an instance of for our running example:EqCat
final case class Cat(name: String, age: Int, color: String)
implicit val catEq: Eq[Cat] = Eq.instance[Cat] { (cat1, cat2) =>
  (cat1.name  === cat2.name ) &&
    (cat1.age   === cat2.age  ) &&
    (cat1.color === cat2.color)
}

val cat1 = Cat("Garfield",   38, "orange and black")
val cat3 = Cat("Heathcliff", 33, "orange and black")

val optionCat1 = Option(cat1)
val optionCat2 = Option.empty[Cat]
val optionCat3 = Option(cat3)

optionCat1 === optionCat2
optionCat1 === optionCat3