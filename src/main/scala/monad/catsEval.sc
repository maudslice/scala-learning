import cats.Eval

// Now, Always, Later是Eval的三个子类
// Now对应严格求值, call-by-value, 会立即对表达式进行计算并缓存, 等同于使用val
val now = Eval.now(math.random() + 1000)

// Always对应惰性求值, call-by-name, 等同于使用() => A, 不会缓存, 每次调用都会重新计算
val always = Eval.always(math.random() + 2000)

// Later对应带缓存的惰性求值, call-by-need, 等同于使用lazy val
val later = Eval.later(math.random() + 3000)

// 使用value方法提取Eval的结果
now.value
always.value
later.value

// 严格求值模型, 并且进行缓存
val x = Eval.now {
  println("computing x")
  math.random()
}
//computing x
//val x: cats.Eval[Double] = Now(0.21585332355497622)

// 两次访问的值相同, 并且是使用的缓存的值
x.value
//val res3: Double = 0.21585332355497622
x.value
//val res4: Double = 0.21585332355497622

// 惰性求值模型, 不进行缓存
val y = Eval.always {
  println("Computing Y")
  math.random
}
//val y: cats.Eval[Double] = cats.Always@217b0ad4

// 每次访问y.value都会重新进行求值
y.value
//Computing Y
//val res5: Double = 0.48948398678931027
y.value
//Computing Y
//val res6: Double = 0.7182046204016904

// 惰性求值模型, 进行缓存
val z = Eval.later {
  println("Computing Z")
  math.random
}
//val z: cats.Eval[Double] = cats.Later@2d150b5

// 第一次访问z.value时才进行求值, 并且缓存
z.value
//Computing Z
//val res7: Double = 0.17309197353375616
z.value
//val res8: Double = 0.17309197353375616

// 总结
/*
Scala             Cats           Properties
 val               Now            eager, memoized
 def               Always         lazy, not memoized
 lazy val          Later          lazy, memoized
 */

// Eval同时也是一个Monad, 可以使用map和flatmap将计算追加到链上, 这些已函数列表的形式存储, 等到调用Eval.value的时候才会被执行
val greeting = Eval
  .always{ println("Step 1"); "Hello" }
  .map{ str => println("Step 2"); s"$str world" }
//val greeting: cats.Eval[String] = cats.Eval$$anon$4@74a424e7

greeting.value
//Step 1
//Step 2
//val res9: String = Hello world

// 虽然保持了原始Eval实例的语义, 但map函数总是需要按照惰性调用(def 语义)
val ans = for {
  a <- Eval.now{ println("Calculating A"); 40 } // flatmap
  b <- Eval.always{ println("Calculating B"); 2 }  //
} yield {
  println("Adding A and B")
  a + b
}
// a被立即求值, 并且缓存
//Calculating A
//val ans: cats.Eval[Int] = cats.Eval$$anon$4@23d4c9d0

ans.value
// 第一次调用, b被求值, 不缓存
//Calculating B
//Adding A and B // a + b 被求值
//val res10: Int = 42

ans.value
// 第二次调用, b被求值
// Calculating B
//Adding A and B // a + b被求值
//val res11: Int = 42

// Eval提供了memoize 方法, 可以对一连串的计算结果进行记忆
val saying = Eval
  .always{ println("Step 1"); "The cat" }
  .map{ str => println("Step 2"); s"$str sat on" }
  .memoize
  .map{ str => println("Step 3"); s"$str the mat" }

// 第一次调用step1, 2, 3均被求值
saying.value

// 第二次调用, 只有memoize后面的step3被求值, 类似spark rdd checkpoint
saying.value

// Eval实现的map和flatmap使用了Trampolining技术, 所以使用它们是堆栈安全的
def factorial(n: BigInt): BigInt =
  if(n == 1) n else n * factorial(n - 1)

//factorial(50000) //StackOverflowError

// 使用Eval重写堆栈安全的factorial2
def factorial2(n: BigInt): Eval[BigInt] =
  if(n == 1) Eval.now(1)
  else factorial2(n - 1).map(_ * n)

//factorial2(50000) //StackOverflowError
// factorial2仍然StackOverflowError了, 这是因为在调用Eval.map方法前, 就已经对factorial2进行了递归调用
// 使用Eval.defer来解决这个问题, 它接收一个Eval实例被将其延迟求值, defer同样是Trampolining的
def factorial3(n: BigInt): Eval[BigInt] =
  if(n == 1) {
    Eval.now(n)
  } else {
    Eval.defer(factorial3(n - 1).map(_ * n))
  }

factorial3(50000) // 能够计算

// 总结
// Eval是一个有用的工具, 在处理非常大的计算和数据结构的时候, 可以保证stack安全. 但是Trampolining并不是没有代价的, 它通过在heap上创建一个函数调用链来避免消耗stack, 它仍然对计算的嵌套深度有限制, 这个限制由heap的大小而不是stack的大小来决定.