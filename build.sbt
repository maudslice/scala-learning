ThisBuild / scalaVersion := "2.13.10"
ThisBuild / organization := "com.maud"

lazy val cats = (project in file("."))
  .settings(
    name := "cats",
    libraryDependencies += "org.typelevel" %% "cats-core" % "2.4.2",
    libraryDependencies += "com.eed3si9n" %% "gigahorse-okhttp" % "0.5.0",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.7" % Test,

    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      "-feature",
      "-language:_"
    )
  )


