scalaVersion := "2.10.2"

name := "PsyDrive"

organization := "Psywerx"

scalacOptions ++= Seq("-optimize")

resolvers += "linter" at "http://hairyfotr.github.io/linteRepo/releases"

addCompilerPlugin("com.foursquare.lint" %% "linter" % "0.1-SNAPSHOT")

seq(lwjglSettings: _*)
