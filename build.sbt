ThisBuild / scalaVersion      := "2.13.10"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision
ThisBuild / scalacOptions += "-Ywarn-unused"
onLoad in Global := {
    Command.process("project fusion", _: State)
} compose (onLoad in Global).value

lazy val fusion = (project in file("./packages/fusion"))
