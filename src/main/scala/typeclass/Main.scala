package typeclass

import java.util.Date

/**
 * @date 2023-03-22 15:03
 * @author chenzhr
 * @Description
 */
object Main {
  final case class Cat(name: String, age: Int, color: String)
  def main(args: Array[String]): Unit = {
//    usePrintable()
    useCatsAsPrintable()
  }

  def usePrintable(): Unit = {
    import PrintableSyntax._
    val aCat = Cat("xiaohua", 15, "red")
    123.print
    "kaka".print
    aCat.print
  }

  def useCatsAsPrintable(): Unit = {
    import cats.Show
    import cats.instances.int._ // for Show
    import cats.instances.string._
    Show[Int].show(123)
    Show[String].show("kaka")

    // 导入接口语法, 向任意类型注入show方法
    import cats.syntax.show._
    123.show
    "kaka".show

    // 自定义Show的typeclass实例
    implicit val dateShow: Show[Date] = (t: Date) => s"${t.getTime}ms since the epoch."


    // 使用其他更加简便的方法来自自定义实例
    // 定义两个相同的Show[Date]实例将会导致编译器无法为Date类型引入隐式转换
//    implicit val dataShow2: Show[Date] = Show.show(date => s"${date.getTime}ms since the epoch.")

    val date = new Date()
    println(date.show)

    implicit val catShow: Show[Cat] = Show.show[Cat] { cat =>
      val name = cat.name.show
      val age = cat.age.show
      val color = cat.color.show
      s"$name is a $age year-old $color cat."
    }
    val cat = Cat("xiaohong", 12, "yellow")
    println(cat.show)
  }

}
