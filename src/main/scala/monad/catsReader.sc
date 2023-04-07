import cats.data.Reader

final case class Cat(name: String, favoriteFood: String)

// 使用Reader.apply方法创建Reader实例
val catName: Reader[Cat, String] = Reader(cat => cat.name)
// 可以再次使用Reader的run方法提取函数，并像往常一样使用apply调用它
catName.run(Cat("Garfield", "lasagne"))
// val res0: cats.Id[String] = Garfield

// Reader强大的地方在于可以使用map和flatmap方法来组合不同的Reader, 最后调用run方法来同样注入所有配置
val greetKitty: Reader[Cat, String] = catName.map(name => s"Hello $name")
greetKitty.run(Cat("Heathcliff", "junk food"))
// val res1: cats.Id[String] = Hello Heathcliff

val feedKitty: Reader[Cat, String] = Reader(cat => s"Have a nice bowl of ${cat.favoriteFood}")
// 组合两个的Reader
val greetAndFeed: Reader[Cat, String] = for {
  greet <- greetKitty
  feed <- feedKitty
} yield s"$greet. $feed."
greetAndFeed(Cat("Garfield", "lasagne"))
// res3: cats.package.Id[String] = "Hello Garfield. Have a nice bowl of lasagne."
greetAndFeed(Cat("Heathcliff", "junk food"))
// res4: cats.package.Id[String] = "Hello Heathcliff. Have a nice bowl of junk food."