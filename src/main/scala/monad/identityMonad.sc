import cats.Monad
import cats.syntax.functor._ // for map
import cats.syntax.flatMap._ // for flatMap

def sumSquare[F[_] : Monad](a: F[Int], b: F[Int]): F[Int] =
  for {
    x <- a
    y <- b
  } yield x * x + y * y

//sumSquare(3, 4) // error, sumSquare的参数只能是Monad, 不能向里面传递普通的值

// cats提供了Id类型来让不是Monad的参数也能够使用sumSquare
import cats.Id
sumSquare(3: Id[Int], 4: Id[Int])

// Id实际上是一个类型别名: type Id[A] = A
// 它将一个原子类型变成一个单参数构造器, 这样就可以把任何类型的值都投射到相应的Id上
"kaka!": Id[String]
123: Id[Int]
List(1, 2, 3): Id[List[Int]]

// cats 为Id提供了各种typeclass实例, 包括Functor和Monad
val a = Monad[Id].pure(3)
val b = Monad[Id].flatMap(a)(_ + 2)

for {
  x <- a
  y <- b
} yield x + y