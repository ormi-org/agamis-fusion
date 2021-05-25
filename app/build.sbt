name := "fusion"

version := "1.0"

scalaVersion := "2.13.5"

lazy val akkaVersion = "2.6.13"
val akkaHttpVersion = "10.2.4"
lazy val igniteVersion = "2.10.0"

resolvers += "GridGain External Repository" at "https://www.gridgainsystems.com/nexus/content/repositories/external"

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.2",
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion,
  "org.apache.ignite" % "ignite-core" % igniteVersion,
  "org.apache.ignite" % "ignite-spring" % igniteVersion,
  "org.gridgain" % "control-center-agent" % "2.9.0.1",
  "org.reactivemongo" %% "reactivemongo" % "1.0.3",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  // Testing 
  "org.scalactic" %% "scalactic" % "3.2.7",
  "org.scalatest" %% "scalatest" % "3.2.7" % "test", 
  // JWT
  "com.github.jwt-scala" %% "jwt-core" % "7.1.5",
  "com.github.jwt-scala" %% "jwt-spray-json" % "7.1.5"

)
