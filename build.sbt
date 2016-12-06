tutSettings

githubProject := "akka-http-phoenix"

crossScalaVersions :=
    "2.11.8" ::
    "2.12.1" ::
    Nil

javacOptions ++=
    "-source" :: "1.7" ::
    "-target" :: "1.7" ::
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

tutTargetDirectory := baseDirectory.value