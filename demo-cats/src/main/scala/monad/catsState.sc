import cats.data.State

// State[S, A]的示例表示一个S => (S, A)的函数, S是状态的类型, A是结果的类型
val a = State[Int, String] { state => (state, s"The state is $state") }

// State实例实际上是一个函数, 它主要做两件事:
// 把一个输入的State转换为输出State
// 计算出结果
// 从一个State获得结果和状态
val (state, result) = a.run(10).value
// state: Int = 10
// result: String = "The state is 10"

// 只取状态
val justTheState = a.runS(10).value

// 只取结果
val justTheRes = a.runA(10).value

// 使用map和flatmap对State进行组合和转换
val step1 = State[Int, String] { num =>
  val ans = num + 1
  (ans, s"Result of step1: $ans")
}

val step2 = State[Int, String] { num =>
  val ans = num * 2
  (ans, s"Result of step2: $ans")
}

val both = for {
  a <- step1
  b <- step2
} yield (a, b)

val (state2, res) = both.run(20).value
// state2: Int = 42
// result: (String, String) = ("Result of step1: 21", "Result of step2: 42")

// State的一般使用模式
// 使用get返回不经修改的状态
val getDemo = State.get[Int]
// getDemo: State[Int, Int] = cats.data.IndexedStateT@741518c8
getDemo.run(10).value
// res1: (Int, Int) = (10, 10)

// 使用set设置状态并返回
val setDemo = State.set[Int](30)
// setDemo: State[Int, Unit] = cats.data.IndexedStateT@509fb0a
setDemo.run(10).value
// res2: (Int, Unit) = (30, ())

// 使用pure忽略状态并返回提供的结果
val pureDemo = State.pure[Int, String]("Result")
// pureDemo: State[Int, String] = cats.data.IndexedStateT@562ae0a8
pureDemo.run(10).value
// res3: (Int, String) = (10, "Result")

// 使用inspect通过一个函数修改State的值
val inspectDemo = State.inspect[Int, String](x => s"${x}!")
// inspectDemo: State[Int, String] = cats.data.IndexedStateT@2dc6b50f
inspectDemo.run(10).value
// res4: (Int, String) = (10, "10!")

// 使用modify通过一个函数更新状态
val modifyDemo = State.modify[Int](_ + 1)
// modifyDemo: State[Int, Unit] = cats.data.IndexedStateT@71c93b27
modifyDemo.run(10).value
// res5: (Int, Unit) = (11, ())

import State._

val program: State[Int, (Int, Int, Int)] = for {
  a <- get[Int]
  _ <- set[Int](a + 1)
  b <- get[Int]
  _ <- modify[Int](_ + 1)
  c <- inspect[Int, Int](_ * 1000)
} yield (a, b, c)
// program: State[Int, (Int, Int, Int)] = cats.data.IndexedStateT@3b525fbf

val (state3, result3) = program.run(1).value
// state3: Int = 3
// result: (Int, Int, Int) = (1, 2, 3000)
