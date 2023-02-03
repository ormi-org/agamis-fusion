ThisBuild / scalaVersion := "2.13.10"
bloopAggregateSourceDependencies in Global := true

lazy val root = (project in file("."))
    .settings(
        name := "fusion"
    )
    .aggregate(
        fusionCore
    )
    
lazy val fusionCore = project.in(file("./packages/fusion-core"))