import cats.data.Reader
import cats.syntax.applicative._ // for pure

/**
 *
 * @param usernames 有效用户列表
 * @param passwords 用户的密码列表
 */
final case class Db(
                     usernames: Map[Int, String],
                     passwords: Map[String, String]
                   )

type DbReader[A] = Reader[Db, A]

// 根据id查询user, 返回DbReader
def findUsername(userId: Int): DbReader[Option[String]] =
  Reader(db => db.usernames.get(userId))

// 查询用户密码
def checkPassword(username: String, password: String): DbReader[Boolean] =
  Reader(db => db.passwords.get(username).contains(password))

// 检查使用给定的用户和密码登录
def checkLogin(userId: Int, password: String): DbReader[Boolean] =
  findUsername(userId)
    .flatMap(name => name.map(checkPassword(_, password)).getOrElse(false.pure[DbReader]))

def checkLogin2(userId: Int, password: String): DbReader[Boolean] = for {
  username   <- findUsername(userId)
  passwordOk <- username.map { username =>
    checkPassword(username, password)
  }.getOrElse {
    false.pure[DbReader]
  }
} yield passwordOk

// 使用
val users = Map(
  1 -> "dade",
  2 -> "kate",
  3 -> "margo"
)

val passwords = Map(
  "dade"  -> "zerocool",
  "kate"  -> "acidburn",
  "margo" -> "secret"
)

val db = Db(users, passwords)

checkLogin(1, "zerocool").run(db)
// res7: cats.package.Id[Boolean] = true
checkLogin(4, "davinci").run(db)
// res8: cats.package.Id[Boolean] = false