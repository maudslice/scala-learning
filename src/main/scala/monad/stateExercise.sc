// 使用State实现一个后序的整数算数计算器
// 从左到右遍历符号，一边走一边带着操作数的堆栈
/*
1 2 + 3 * // see 1, push onto stack
2 + 3 *   // see 2, push onto stack
+ 3 *     // see +, pop 1 and 2 off of stack,
          //        push (1 + 2) = 3 in their place
3 3 *     // see 3, push onto stack
3 *       // see 3, push onto stack
*         // see *, pop 3 and 3 off of stack,
          //        push (3 * 3) = 9 in their place
 */
// 写一个解释器, 把每个符号都解析成一个State, 代表堆栈上的转换和中间结果
// 用flatmap串起来, 可以产生一个可用于任何序列的解释器

import cats.data.State

type CalcState[A] = State[List[Int], A]

def operator(f: (Int, Int) => Int): CalcState[Int] = {
  State[List[Int], Int] {
    case b :: a :: tail =>
      val ans = f(a, b)
      (ans :: tail, ans)
    case _ => sys.error("failed!")
  }
}

def operand(num: Int): CalcState[Int] = {
  State[List[Int], Int] { stack =>
    (num :: stack, num)
  }
}
// 将一个符号解析为一个State
def evalOne(sym: String): CalcState[Int] = sym match {
  case "+" => operator(_ + _)
  case "-" => operator(_ - _)
  case "*" => operator(_ * _)
  case "/" => operator(_ / _)
  case num => operand(num.toInt)
}


// 使用evalOne
evalOne("42").runA(Nil).value
// res0: Int = 42

// 更复杂的程序
val program = for {
  _ <- evalOne("1")
  _ <- evalOne("2")
  ans <- evalOne("+")
} yield ans

program.runA(Nil).value
// val res1: Int = 3

// 计算List[String]的结果

import cats.syntax.applicative._ // for pure

def evalAll(input: List[String]): CalcState[Int] =
  input.foldLeft(0.pure[CalcState]) { (acc, b) => acc.flatMap(_ => evalOne(b)) }

evalAll(List("1", "2", "+", "3", "*")).runA(Nil).value

// evalAll 和 evalOne都是纯函数, 也可以用flatmap进行串联, 来处理更加复杂的情况
val biggerProgram = for {
  _   <- evalAll(List("1", "2", "+"))
  _   <- evalAll(List("3", "4", "+"))
  ans <- evalOne("*")
} yield ans

biggerProgram.runA(Nil).value
// val res3: Int = 21

// 将输入的input分割成符号, 调用evalAll, 并在初始堆栈上运行
def evalInput(input: String): Int = {
  evalAll(input.split(" ").toList).runA(Nil).value
}

evalInput("1 2 + 3 4 + *")
// val res4: Int = 21
