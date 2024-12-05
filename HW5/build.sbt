ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.15"

import scala.collection.Seq
import sbt.Keys.libraryDependencies
import sbt.Resolver

lazy val root = (project in file("."))
  .settings(
    name := "HW5",
    idePackagePrefix := Some("app-hw5"),
    resolvers += Resolver.mavenLocal,
    libraryDependencies ++= Seq(
      "org.apache.kafka" %% "kafka-streams-scala" % "3.8.0",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
      "ch.qos.logback" % "logback-classic" % "1.5.6",
      "com.typesafe.akka" %% "akka-stream" % "2.8.6",
      "com.typesafe.akka" %% "akka-stream-kafka" % "4.0.2"
    )
  )