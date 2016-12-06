githubProject := "akka-http-phoenix"

crossScalaVersions :=
    "2.11.8" ::
    "2.12.1" ::
    Nil

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

scalaVersion := "2.12.1"

startYear := Some( 2016 )

testOptions in Test += Tests.Argument( "-oFD" )