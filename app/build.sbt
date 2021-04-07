name := "fusion"

version := "1.0"

scalaVersion := "2.13.5"

lazy val akkaVersion = "2.6.13"
lazy val igniteVersion = "2.10.0"

resolvers += "GridGain External Repository" at "https://www.gridgainsystems.com/nexus/content/repositories/external"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.2",
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion,
  "org.apache.ignite" % "ignite-core" % igniteVersion,
  "org.apache.ignite" % "ignite-spring" % igniteVersion,
  "org.gridgain" % "control-center-agent" % "2.9.0.1",
  "org.reactivemongo" %% "reactivemongo" % "1.0.3",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.1.0" % Test
)
