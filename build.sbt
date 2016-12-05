githubProject := "akka-http-phoenix"

javacOptions ++=
    "-source" :: "1.7" ::
    "-target" :: "1.7" ::
    Nil

libraryDependencies ++=
    "com.typesafe.akka" %% "akka-http" % "10.0.0" ::
    "io.circe" %% "circe-core" % "0.6.1" ::
    "io.circe" %% "circe-generic" % "0.6.1" ::
    "io.circe" %% "circe-parser" % "0.6.1" ::
    "org.scalatest" %% "scalatest" % "3.0.1" % "test" ::
    Nil

name := "akka-http-phoenix"

organization := "io.taig"

scalacOptions ++=
    "-deprecation" ::
    "-feature" ::
    Nil

scalaVersion := "2.11.8"

startYear := Some( 2016 )

testOptions in Test += Tests.Argument( "-oFD" )