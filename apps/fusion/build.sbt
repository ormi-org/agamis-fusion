ThisBuild / scalaVersion      := "2.13.10"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

lazy val pekkoVersion           = "1.0.1"
lazy val pekkoHttpVersion       = "1.0.0"
lazy val igniteVersion          = "2.14.0"
lazy val pekkoManagementVersion = "1.0.0"

val jacksonVersion = "2.14.2"

lazy val fusion = (project in file("."))
    .settings(
      name            := "fusion",
      version         := "0.1.0",
      autoAPIMappings := true,
      scalacOptions ++= Seq(
        "-encoding",
        "utf8",
        "-feature",
        "-deprecation",
        "-language:implicitConversions",
        "-language:existentials",
        "-unchecked",
        "-Xlint",
        "-Ywarn-unused:imports",
        "-Xlint:-unused,_"
      ),
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
        "--add-opens=java.base/java.lang=ALL-UNNAMED",
        "--add-opens=java.base/java.time=ALL-UNNAMED"
      ),
      libraryDependencies ++= Seq(
        "org.scala-lang.modules" %% "scala-parallel-collections" % "1.0.2",
        "org.apache.pekko" %% "pekko-http"            % pekkoHttpVersion,
        "org.apache.pekko" %% "pekko-http-spray-json" % pekkoHttpVersion,
        "org.apache.pekko" %% "pekko-stream-testkit"  % pekkoVersion,
        "org.apache.pekko" %% "pekko-http-testkit"    % pekkoHttpVersion,
        "org.scala-lang"    % "scala-reflect"         % scalaVersion.value,
        "org.apache.pekko" %% "pekko-serialization-jackson"  % pekkoVersion,
        "org.apache.pekko" %% "pekko-actor-typed"            % pekkoVersion,
        "org.apache.pekko" %% "pekko-cluster-typed"          % pekkoVersion,
        "org.apache.pekko" %% "pekko-cluster-sharding"       % pekkoVersion,
        "org.apache.pekko" %% "pekko-cluster-sharding-typed" % pekkoVersion,
        "org.apache.pekko" %% "pekko-cluster-metrics"        % pekkoVersion,
        "org.apache.pekko" %% "pekko-discovery-kubernetes-api" % pekkoManagementVersion,
        "org.apache.pekko" %% "pekko-management-cluster-bootstrap" % pekkoManagementVersion,
        "org.apache.pekko" %% "pekko-management-cluster-http" % pekkoManagementVersion,
        "org.apache.ignite"  % "ignite-core"     % igniteVersion,
        "org.apache.ignite"  % "ignite-spring"   % igniteVersion,
        "org.reactivemongo" %% "reactivemongo"   % "1.0.3",
        "ch.qos.logback"     % "logback-classic" % "1.2.3",
        "org.apache.pekko" %% "pekko-actor-testkit-typed" % pekkoVersion % Test,
        // Testing
        "org.scalactic" %% "scalactic" % "3.2.7",
        "org.scalatest" %% "scalatest" % "3.2.7" % Test,
        "org.scalamock" %% "scalamock" % "5.1.0" % Test,
        // JWT
        "com.github.jwt-scala" %% "jwt-core"       % "7.1.5",
        "com.github.jwt-scala" %% "jwt-spray-json" % "7.1.5",
        // Jackson
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion,
        // Bcrypt
        "at.favre.lib" % "bcrypt" % "0.9.0",
        // JAVET
        "com.caoccao.javet" % "javet" % "2.2.1"
      )
    )
