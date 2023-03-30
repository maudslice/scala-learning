import cats.syntax.either._

type Result[A] = Either[Throwable, A]
// Result的语义类似scala.util.Try, 但Throwable是一个极为宽泛的类型, 并不知道发生了什么错误


// 用ADT来代表可能出现错误类型
sealed trait LoginError extends Product with Serializable

final case class UserNotFound(username: String) extends LoginError

final case class PasswordIncorrect(username: String) extends LoginError

case object UnexpectedError extends LoginError

case class User(username: String, password: String)

type LoginResult = Either[LoginError, User]

// 使用模式匹配对可能出现的错误进行穷举
def handleError(error: LoginError): Unit =
  error match {
    case UserNotFound(u) =>
      println(s"User not found: $u")

    case PasswordIncorrect(u) =>
      println(s"Password incorrect: $u")

    case UnexpectedError =>
      println(s"Unexpected error")
  }

val result1: LoginResult = User("dave", "passw0rd").asRight
val result2: LoginResult = UserNotFound("dave").asLeft

result1.fold(handleError, println)
result2.fold(handleError, println)