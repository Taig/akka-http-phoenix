val akka = "10.0.0"

val circe = "0.6.1"

val scalatest = "3.0.1"

libraryDependencies ++=
    "com.typesafe.akka" %% "akka-http" % akka ::
    "io.circe" %% "circe-core" % circe ::
    "io.circe" %% "circe-generic" % circe ::
    "io.circe" %% "circe-parser" % circe ::
    "org.scalatest" %% "scalatest" % scalatest % "test" ::
    Nil