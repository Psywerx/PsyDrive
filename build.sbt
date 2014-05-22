name := "PsyDrive"

organization := "Psywerx"

scalaVersion := "2.11.1"

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

scalacOptions ++= Seq(
  "-optimize",
  "-Yinline",
  "-Yclosure-elim")

seq(lwjglSettings: _*)
