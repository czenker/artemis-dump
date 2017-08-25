import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.12.1",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "Hello",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "org.scala-lang.modules" % "scala-xml_2.12" % "1.0.6",
    libraryDependencies += "com.beachape" %% "enumeratum" % "1.5.12"
  )
