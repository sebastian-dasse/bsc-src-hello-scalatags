enablePlugins(ScalaJSPlugin)            // Turn this project into a Scala.js project by importing these settings

name := "hello-scalatags"

version := "1.0"

scalaVersion := "2.11.7"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint"
)

scalaJSStage in Global := FastOptStage

EclipseKeys.withSource := true
