
lazy val commonSettings = Seq(
  organization := "org.vaslabs",
  scalaVersion := "2.12.4"
)

lazy val `service-model` = project.in(file("service-model"))
  .settings(commonSettings: _*)
  .settings(
    name := "maite-model",
    libraryDependencies ++= Dependencies.`service-model`
  )

lazy val `geo-cache` = project.in(file("geo-cache"))
    .settings(commonSettings: _*)
    .settings(
      name := "maite-geo-cache",
      libraryDependencies ++= Dependencies.`geo-cache`
    )

lazy val maite = project.in(file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "maite"
  )
  .aggregate(`service-model`, `geo-cache`)