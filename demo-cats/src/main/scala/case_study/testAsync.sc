import scala.concurrent.Future

// 轮询远程服务器正常运行的时间
trait UptimeClient {
  def getUptime(hostname: String): Future[Int]
}

import cats.instances.future._
import cats.instances.list._
import cats.syntax.traverse._

import scala.concurrent.ExecutionContext.Implicits.global

// 维护一个服务器列表, 允许用户轮询它们的总运行时间
class UptimeService(client: UptimeClient) {
  def getTotalUptime(hostnames: List[String]): Future[Int] =
    hostnames.traverse(client.getUptime).map(_.sum)
}

// 测试客户端, 允许提供假数据, 而不是调用实际的服务器
class TestUptimeClient(hosts: Map[String, Int]) extends UptimeClient {
  def getUptime(hostname: String): Future[Int] =
    Future.successful(hosts.getOrElse(hostname, 0))
}

// 假设对UptimeService进行单元测试, 测试这个service对数值进行求和的能力, 不管这些数值从哪来
def testTotalUptime(): Unit = {
  val hosts = Map("host1" -> 10, "host2" -> 6)
  val client = new TestUptimeClient(hosts)
  val service = new UptimeService(client)
  val actual = service.getTotalUptime(hosts.keys.toList)
  val expected = hosts.values.sum
  // getTotalUptime是异步的, 结果类型是Future[Int], 不能直接进行比较
  // 有几种办法可以解决这个问题, 可以修改代码来让测试代码适应异步
  // 也可以把service代码变成同步的, 这样就不用修改测试代码了
  assert(actual == expected)
}

// 实现两个版本的UptimeClient：一个异步的用于生产，一个同步的用于单元测试:

// 问题是, 该如何对Future[Int]和Int进行抽象, 用原来的UptimeClient是无法通过编译的
/*
trait RealUptimeClient extends UptimeClient {
  def getUptime(hostname: String): Future[Int]
}

trait TestUptimeClient extends UptimeClient {
  def getUptime(hostname: String): Int
}

 */
// 答案是使用cats.Id, Id允许在类型构造器中包装类型而不改变它的含义
// type Id[A] = A
// 现在重新定义trait UptimeClient, 让两个client都extends UptimeClient, 分别把F绑定到Future和Id上

import cats.Id

trait UptimeClient2[F[_]] {
  def getUptime(hostname: String): F[Int]
}

trait RealUptimeClient extends UptimeClient2[Future] {
  def getUptime(hostname: String): Future[Int]
}

trait TestUptimeClient extends UptimeClient2[Id] {
  // 因为Id[Int]只是Int的别名, 所以返回类型可以写成Int
  def getUptime(hostname: String): Int
}

// 最后, 重新实现TestUptimeClient, 接受一个Map作为参数
class TestUptimeClient2(hosts: Map[String, Int]) extends UptimeClient2[Id] {
  override def getUptime(hostname: String): Int = hosts.getOrElse(hostname, 0)
}

// 现在, 重写UptimeService, 抽象出UptimeClient2的两种类型
// 重写类, 方法签名, 和方法体

import cats.Applicative
import cats.syntax.functor._ // for map

// 隐式参数语法
class UptimeService2[F[_]](client: UptimeClient2[F])
                          (implicit a: Applicative[F]) {
  // 注意，需要导入cats.syntax.functor以及cats.Applicative。这是因为要从使用future.map切换到需要一个implicit的Functor参数的Cats的通用扩展方法。
  def getTotalUptime(hostnames: List[String]): F[Int] = {
    // traverse只对Applicative的秩序列起作用
    // 所以需要告诉编译器, F是一个Applicative
    // 可以通过隐式参数或上下文绑定来实现
    hostnames.traverse(client.getUptime).map(_.sum)
  }
}

// 上下文绑定语法
class UptimeService2[F[_] : Applicative](client: UptimeClient2[F]) {
  def getTotalUptime(hostnames: List[String]): F[Int] =
    hostnames.traverse(client.getUptime).map(_.sum)
}

// 现在, 不需要对测试代码进行任何的更改, 就能运行单元测试
def testTotalUptime2(): Unit = {
  val hosts = Map("host1" -> 10, "host2" -> 6)
  val client = new TestUptimeClient2(hosts)
  val service = new UptimeService2(client)
  val actual = service.getTotalUptime(hosts.keys.toList)
  val expected = hosts.values.sum
  assert(actual == expected)
}

testTotalUptime2()






