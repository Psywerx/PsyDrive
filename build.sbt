name := "PsyDrive"

organization := "Psywerx"

scalaVersion := "2.11.2"

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

libraryDependencies += "org.clojure" % "clojure" % "1.6.0"

libraryDependencies += "org.apache.commons" % "commons-math3" % "3.3"

fork := true

scalacOptions ++= Seq(
  "-optimize",
  "-Yinline",
  "-Yclosure-elim")

seq(lwjglSettings: _*)

