ThisBuild / scalaVersion := "2.13.10"

lazy val akkaVersion = "2.7.0"
lazy val akkaHttpVersion = "10.4.0"
lazy val igniteVersion = "2.10.0"
lazy val akkaManagementVersion = "1.2.0"

val jacksonVersion = "2.14.2"

lazy val fusion = (project in file("."))
  .settings(
    name := "fusion",
    version := "0.1.0",
    autoAPIMappings := true,
    scalacOptions ++= Seq("-feature", "-deprecation"),
    javaOptions ++= Seq(
      "--add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED",
      "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED",
      "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
      "--add-opens=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED",
      "--add-opens=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED",
      "--add-opens=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED",
      "--add-opens=java.base/java.io=ALL-UNNAMED",
      "--add-opens=java.base/java.nio=ALL-UNNAMED",
      "--add-opens=java.base/java.util=ALL-UNNAMED",
      "--add-opens=java.base/java.lang=ALL-UNNAMED"
    ),
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.2",
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion,
      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion,
      "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % akkaManagementVersion,
      "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % akkaManagementVersion,
      "com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaManagementVersion,
      "org.apache.ignite" % "ignite-core" % igniteVersion,
      "org.apache.ignite" % "ignite-spring" % igniteVersion,
      "org.reactivemongo" %% "reactivemongo" % "1.0.3",
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
      // Testing 
      "org.scalactic" %% "scalactic" % "3.2.7",
      "org.scalatest" %% "scalatest" % "3.2.7" % "test", 
      // JWT
      "com.github.jwt-scala" %% "jwt-core" % "7.1.5",
      "com.github.jwt-scala" %% "jwt-spray-json" % "7.1.5",
      // Jackson
      "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
      // "com.fasterxml.jackson.datatype" %% "jackson-datatype-jsr310" % jacksonVersion,
      // Bcrypt 
      "at.favre.lib" % "bcrypt" % "0.9.0"
    )
  )
