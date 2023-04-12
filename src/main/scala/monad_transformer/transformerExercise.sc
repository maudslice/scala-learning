import cats.data.EitherT

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.DurationInt

//def getPowerLevel(autobot: String): Response[Int] =
//  ???

// 使用这个Response类型有大量的flatmap map嵌套, 不易于理解, 使用monad transformer来简化她
// type Response[A] = Future[Either[String, A]]
type Response[A] = EitherT[Future, String, A]

// usage case
val powerLevels = Map(
  "Jazz" -> 6,
  "Bumblebee" -> 8,
  "Hot Rod" -> 10
)

import cats.instances.future._ // for Monad
import scala.concurrent.ExecutionContext.Implicits.global

def getPowerLevel(ally: String): Response[Int] = {
  powerLevels.get(ally) match {
    case Some(avg) => EitherT.right(Future(avg))
    case None => EitherT.left(Future(s"$ally unreachable"))
  }
}

def canSpecialMove(ally1: String, ally2: String): Response[Boolean] = {
  for {
    p1 <- getPowerLevel(ally1)
    p2 <- getPowerLevel(ally2)
  } yield (p1 + p2) > 15
}

def tacticalReport(ally1: String, ally2: String): String = {
  val stack = canSpecialMove(ally1, ally2).value
  Await.result(stack, 1.second) match {
    case Left(msg) =>
      s"Comms error: $msg"
    case Right(true) =>
      s"$ally1 and $ally2 are ready to roll out!"
    case Right(false) =>
      s"$ally1 and $ally2 need a recharge."
  }
}

tacticalReport("Jazz", "Bumblebee")
// res13: String = "Jazz and Bumblebee need a recharge."
tacticalReport("Bumblebee", "Hot Rod")
// res14: String = "Bumblebee and Hot Rod are ready to roll out!"
tacticalReport("Jazz", "Ironhide")
// res15: String = "Comms error: Ironhide unreachable"

