name := "PsyDrive"

organization := "Psywerx"

scalaVersion := "2.11.1"

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

libraryDependencies += "org.clojure" % "clojure" % "1.6.0"

scalacOptions ++= Seq(
  "-optimize",
  "-Yinline",
  "-Yclosure-elim")

seq(lwjglSettings: _*)
