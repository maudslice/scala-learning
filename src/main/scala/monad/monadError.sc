import cats.MonadError
import cats.instances.either._ // for MonadError

type ErrorOr[A] = Either[String, A]

val monadError = MonadError[ErrorOr, String]

// raiseError和Monad的pure方法一样，只是它创建一个代表失败的实例
val success = monadError.pure(42)
val failure = monadError.raiseError[Int]("badness")

// handleErrorWith是raiseError的补充, 它允许处理一个错误并尝试将其转换为一个成功, 类似Future的recover方法
monadError.handleErrorWith(failure){
  case "badness" => monadError.pure(0)
  case _ => monadError.raiseError("it's not ok")
}

// 如果知道如何处理所有可能的错误:
monadError.handleError(failure){
  case "badness" => 10
  case _ => -1
}

// 使用ensure方法来实现类似过滤器的行为, 不过某个成功的值不满足某个前提, 则触发一个错误
monadError.ensure(success)("number too low!")(_ > 1000)

// cats 对上述方法提供的语法层面的支持
import cats.syntax.applicative._      // for pure
import cats.syntax.applicativeError._ // for raiseError etc
import cats.syntax.monadError._       // for ensure

val success = 42.pure[ErrorOr]
// success: ErrorOr[Int] = Right(42)
val failure = "Badness".raiseError[ErrorOr, Int]
// failure: ErrorOr[Int] = Left("Badness")
failure.handleErrorWith{
  case "Badness" => 256.pure
  case _ => "It's not ok".raiseError
}
// res4: ErrorOr[Int] = Right(256)
success.ensure("Number to low!")(_ > 1000)
// res5: ErrorOr[Int] = Left("Number to low!")

// cats为scala.util.Try也提供了MonadError
import scala.util.Try
import cats.instances.try_._ // for MonadError

val exn = new RuntimeException("it's all gone wrong")
exn.raiseError[Try, Int]