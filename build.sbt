name := "sbt-typelevel-versioning-pvp" //todo change this

ThisBuild / tlBaseVersion := "0.1"
ThisBuild / versionScheme := Some("early-semver")

ThisBuild / crossScalaVersions := Seq("2.12.20")

ThisBuild / organization := "io.isomarcte"
ThisBuild / organizationName := "io.isomarcte"

lazy val root = project.in(file("."))
  .enablePlugins(NoPublishPlugin)
  .aggregate(
  versioning
)

lazy val versioning = project
  .in(file("versioning"))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "versioning"
  )
