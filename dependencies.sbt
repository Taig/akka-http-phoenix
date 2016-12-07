val akka = "10.0.0"

val circe = "0.6.1"

val macroparadise = "2.1.0"

val scalatest = "3.0.1"

addCompilerPlugin( "org.scalamacros" % "paradise" % macroparadise cross CrossVersion.full )

libraryDependencies ++=
    "com.typesafe.akka" %% "akka-http" % akka ::
    "io.circe" %% "circe-core" % circe ::
    "io.circe" %% "circe-generic" % circe ::
    "io.circe" %% "circe-parser" % circe ::
    "org.scalatest" %% "scalatest" % scalatest % "test" ::
    Nil