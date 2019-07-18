name := "store-actor"

version := "0.1"

scalaVersion := "2.12.8"

val akkaVersion = "2.5.23"
val scalatestVersion = "3.0.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalactic" %% "scalactic" % scalatestVersion,
  "org.scalatest" %% "scalatest" % scalatestVersion % "test"
)
