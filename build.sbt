val scala3Version = "3.2.1"
val scala2Version = "2.13.10"

ThisBuild / scalaVersion := scala3Version
ThisBuild / organization := "com.maud"

lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-unchecked",
    "-Xfatal-warnings",
    "-encoding", "UTF-8",
    "-language:strictEquality",
    "-Xmax-inlines:64"
  ),
)

lazy val scalaLearning = (project in file("."))
  .aggregate(
    demoCats,
    demoShapeless,
    demoFpInScala,
    demoCatsEffect,
    demoCatsParse,
    demoFs2
  )
  .enablePlugins(JavaAppPackaging)
  .settings(commonSettings)
  .settings(
    name := "scala-learning",
    crossScalaVersions := Nil,
  )

lazy val demoCats = (project in file("demo-cats"))
  .settings(commonSettings)
  .settings(
    name := "demo-cats",
    libraryDependencies ++= Seq(
      catsCore,
      catsLaws % Test,
    ),
  )

lazy val demoShapeless = (project in file("demo-shapeless"))
  .settings(commonSettings)
  .settings(
    name := "demo-shapeless",
    crossScalaVersions := List(scala2Version, scala3Version),
    libraryDependencies ++= {
//      Seq(
//        shapeless,
//        catsCore,
//        scalaCheck
//      )
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((3, _)) => Seq(
          shapeless,
          catsCore,
          scalaCheck
        )
        case Some((2, _)) => Seq(
          shapeless2,
          catsCore,
          scalaCheck
        )
        case _ => Seq()
      }
    },
    scalacOptions --= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) => Seq(
          "-language:strictEquality",
          "-Xmax-inlines:64"
        )
        case _ => Seq()
      }
    }
  )

lazy val demoFpInScala = (project in file("demo-fp"))
  .settings(commonSettings)
  .settings(
    name := "demo-fp",
    libraryDependencies ++= Seq(
      scalaCheck,
    ),
  )

lazy val demoCatsEffect = (project in file("demo-cats-effect"))
  .settings(commonSettings)
  .settings(
    name := "demo-cats-effect",
    libraryDependencies ++= Seq(
      catsEffect,
      catsEffectTestkit % Test,
      catsEffectTestingSpecs % Test,
      catsEffectTestingScalaTest % Test,
      mUnitCatsEffect % Test,
      weaverCats % Test,
    ),
  )

lazy val demoCatsParse = (project in file("demo-cats-parse"))
  .settings(commonSettings)
  .settings(
    name := "demo-cats-parse",
    libraryDependencies ++= Seq(
      catsParse,
      jawnAst,
    ),
  )

lazy val demoFs2 = (project in file("demo-fs2"))
  .settings(commonSettings)
  .settings(
    name := "demo-fs2",
    libraryDependencies ++= Seq(
      fs2Core,
      fs2IO,
      fs2ReactiveStreams,
      fs2Scodec,
    ),
  )

// functional

val shapelessVersion = "3.3.0"
val shapeless2Version = "2.3.10"
val catsVersion = "2.9.0"
val catsEffectVersion = "3.4.8"
val catsParseVersion = "0.3.9"
val fs2Version = "3.6.1"
val circeVersion = "0.14.4"
val circeFs2Version = "0.14.1"
val monocleVersion = "3.1.0"
val log4CatsVersion = "2.5.0"
val cirisVersion = "3.1.0"
val cirisHoconVersion = "1.0.1"
val refinedCatsVersion = "0.10.1"
val catsStmVersion = "0.13.3"
val spireVersion = "0.18.0"
val squantsVersion = "1.8.3"
val http4sVersion = "1.0.0-M32"
val http4sDomVersion = "1.0.0-M32"
val http4sDropwizardMetricsVersion = "1.0.0-M32"
val http4sJdkHttpClientVersion = "1.0.0-M1"
val doobieVersion = "1.0.0-RC2"
val redis4CatsVersion = "1.4.0"
val jawnAstVersion = "1.4.0"
val zioVersion = "2.0.8"

val shapeless = "org.typelevel" %% "shapeless3-deriving" % shapelessVersion
val shapeless2 = "com.chuusai" %% "shapeless" % shapeless2Version
val catsCore = "org.typelevel" %% "cats-core" % catsVersion
val alleyCatsCore = "org.typelevel" %% "alleycats-core" % catsVersion
val catsEffect = "org.typelevel" %% "cats-effect" % catsEffectVersion withSources() withJavadoc()
val catsParse = "org.typelevel" %% "cats-parse" % catsParseVersion
val fs2Core = "co.fs2" %% "fs2-core" % fs2Version
val fs2IO = "co.fs2" %% "fs2-io" % fs2Version
val fs2ReactiveStreams = "co.fs2" %% "fs2-reactive-streams" % fs2Version
val fs2Scodec = "co.fs2" %% "fs2-scodec" % fs2Version
val circeCore = "io.circe" %% "circe-core" % circeVersion
val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
val circeParser = "io.circe" %% "circe-parser" % circeVersion
val circeFs2 = "io.circe" %% "circe-fs2" % circeFs2Version
val monocleCore = "dev.optics" %% "monocle-core" % monocleVersion
val monocleMacro = "dev.optics" %% "monocle-macro" % monocleVersion
val log4CatsSlf4j = "org.typelevel" %% "log4cats-slf4j" % log4CatsVersion
val ciris = "is.cir" %% "ciris" % cirisVersion
val cirisCirce = "is.cir" %% "ciris-circe" % cirisVersion
val cirisCirceYaml = "is.cir" %% "ciris-circe-yaml" % cirisVersion
val cirisHttp4s = "is.cir" %% "ciris-http4s" % cirisVersion
val cirisRefined = "is.cir" %% "ciris-refined" % cirisVersion
val cirisSquants = "is.cir" %% "ciris-squants" % cirisVersion
// val cirisHocon = "lt.dvim.ciris-hocon" %% "ciris-hocon" % cirisHoconVersion
val refinedCats = "eu.timepit" %% "refined-cats" % refinedCatsVersion
val catsStm = "io.github.timwspence" %% "cats-stm" % catsStmVersion
val spire = "org.typelevel" %% "spire" % spireVersion
val squants = "org.typelevel" %% "squants" % squantsVersion
val http4sDsl = "org.http4s" %% "http4s-dsl" % http4sVersion
val http4sEmberServer = "org.http4s" %% "http4s-ember-server" % http4sVersion
val http4sEmberClient = "org.http4s" %% "http4s-ember-client" % http4sVersion
val http4sCirce = "org.http4s" %% "http4s-circe" % http4sVersion
val http4sScalaTags = "org.http4s" %% "http4s-scalatags" % http4sVersion
val http4sPrometheusMetrics = "org.http4s" %% "http4s-prometheus-metrics" % http4sVersion
val http4sDropwizardMetrics = "org.http4s" %% "http4s-dropwizard-metrics" % http4sDropwizardMetricsVersion
val http4sJdkHttpClient = "org.http4s" %% "http4s-jdk-http-client" % http4sJdkHttpClientVersion
val doobieCore = "org.tpolecat" %% "doobie-core" % doobieVersion
val doobieH2 = "org.tpolecat" %% "doobie-h2" % doobieVersion
val doobieHikari = "org.tpolecat" %% "doobie-hikari" % doobieVersion
val doobiePostgres = "org.tpolecat" %% "doobie-postgres" % doobieVersion
val doobiePostgresCirce = "org.tpolecat" %% "doobie-postgres-circe" % doobieVersion
val doobieScalaTest = "org.tpolecat" %% "doobie-scalatest" % doobieVersion
val redis4CatsEffects = "dev.profunktor" %% "redis4cats-effects" % redis4CatsVersion
val redis4CatsStreams = "dev.profunktor" %% "redis4cats-streams" % redis4CatsVersion
val redis4CatsLog4Cats = "dev.profunktor" %% "redis4cats-log4cats" % redis4CatsVersion
val jawnAst = "org.typelevel" %% "jawn-ast" % jawnAstVersion
val zio = "dev.zio" %% "zio" % zioVersion
val zioStreams = "dev.zio" %% "zio-streams" % zioVersion

// Test
val scalaTestVersion = "3.2.15"
val scalaCheckVersion = "1.17.0"
val scalaTestPlusVersion = "3.2.15.0"
val scalaCheckEffectVersion = "1.0.4"
val catsEffectTestingSpecsVersion = "1.5.0"
val catsEffectTestingScalaTestVersion = "1.5.0"
val mUnitCatsEffectVersion = "1.0.7"
val weaverCatsVersion = "0.8.1"

val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion
val scalaCheck = "org.scalacheck" %% "scalacheck" % scalaCheckVersion
val scalaTestPlusScalaCheck = "org.scalatestplus" %% "scalacheck-1-17" % scalaTestPlusVersion
val scalaCheckEffect = "org.typelevel" %% "scalacheck-effect" % scalaCheckEffectVersion
val scalaCheckEffectMUnit = "org.typelevel" %% "scalacheck-effect-munit" % scalaCheckEffectVersion
val catsLaws = "org.typelevel" %% "cats-laws" % catsVersion
val catsEffectTestkit = "org.typelevel" %% "cats-effect-testkit" % catsEffectVersion
val catsEffectTestingSpecs = "org.typelevel" %% "cats-effect-testing-specs2" % catsEffectTestingSpecsVersion
val catsEffectTestingScalaTest = "org.typelevel" %% "cats-effect-testing-scalatest" % catsEffectTestingScalaTestVersion
val mUnitCatsEffect = "org.typelevel" %% "munit-cats-effect-3" % mUnitCatsEffectVersion
val weaverCats = "com.disneystreaming" %% "weaver-cats" % weaverCatsVersion


