scalaVersion := "2.11.0-RC3"

name := "PsyDrive"

organization := "Psywerx"

scalacOptions ++= Seq(
  "-optimize",
  //"-feature",
  //"-deprecation",
  "-unchecked",
  //"-Xlint",
  "-Ywarn-adapted-args",
  //"-Ywarn-all",
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  //"-Ywarn-numeric-widen",
  "-Ywarn-value-discard")

resolvers += "linter" at "http://hairyfotr.github.io/linteRepo/releases"

addCompilerPlugin("com.foursquare.lint" %% "linter" % "0.1-SNAPSHOT")

seq(lwjglSettings: _*)
