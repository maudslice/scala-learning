// Define a very simple JSON AST
sealed trait Json
final case class JsObject(get: Map[String, Json]) extends Json
final case class JsString(get: String) extends Json
final case class JsNumber(get: Double) extends Json
final case object JsNull extends Json

// The "serialize to JSON" behaviour is encoded in this trait
// define a type class
trait JsonWriter[A] {
  def write(value: A): Json
}

final case class Person(name: String, email: String)

// type class instances
// 通过隐式值实现typeclass
object JsonWriterInstances {
  implicit val stringWriter: JsonWriter[String] =
    (value: String) => JsString(value)

  implicit val personWriter: JsonWriter[Person] =
    (value: Person) => JsObject(Map(
      "name" -> JsString(value.name),
      "email" -> JsString(value.email)
    ))

  // etc...
}


// typecless 需要typeclass的实例才能工作, 通常是一个接受任何typeclass实例作为隐式参数的方法
object Json {
  def toJson[A](value: A)(implicit w: JsonWriter[A]): Json = w.write(value)
}

// use typeclass
{
  import JsonWriterInstances._
  Json.toJson(Person("kaka", "kaka@gmail.com"))
  Json.toJson("kakaka!")
}

// 通过隐式类实现typeclass
object JsonSyntax {
  implicit class JsonWriterOps[A](value: A) {
    def toJson(implicit w: JsonWriter[A]): Json =
      w.write(value)
  }
}

{
  import JsonWriterInstances._
  import JsonSyntax._
  Person("Dave", "dave@example.com").toJson
}