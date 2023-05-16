import cats.MonadError

import scala.util.Try

def validateAdult[F[_]](age: Int)(implicit me: MonadError[F, Throwable]): F[Int] =
  ???

// 如果age>18, 返回success
def validateAdult[F[_]](age: Int)(implicit me: MonadError[F, Throwable]): F[Int] =
  if(age > 18) me.pure(age)
  else me.raiseError(new IllegalAccessException("Age must be greater than or equal to 18"))

validateAdult[Try](20)
validateAdult[Try](15)

type ExceptionOr[A] = Either[Throwable, A]
validateAdult[ExceptionOr](12)
