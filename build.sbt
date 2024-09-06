name := "sbt-typelevel"

ThisBuild / tlVBaseVersion := "0.4"
ThisBuild / versionScheme := Some("early-semver")

ThisBuild / crossScalaVersions := Seq("2.12.15")

lazy val root = project.in(file("."))
  .enablePlugins(NoPublishPlugin)
  .aggregate(
  kernel,
  versioning
)

lazy val kernel = project
  .in(file("kernel"))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-typelevel-kernel"
  )

lazy val versioning = project
  .in(file("versioning"))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "sbt-typelevel-versioning"
  )
  .dependsOn(kernel)
