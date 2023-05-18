// for demo-http4s deployment 需要有一个main方法，然后执行sbt assembly
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.1.1")
// for demo-fs2-grpc
addSbtPlugin("org.typelevel" % "sbt-fs2-grpc" % "2.5.10")
addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.15")