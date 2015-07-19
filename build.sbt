name := "PsyDrive"

organization := "Psywerx"

scalaVersion := "2.11.7"

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

libraryDependencies += "org.clojure" % "clojure" % "1.7.0"

libraryDependencies += "org.apache.commons" % "commons-math3" % "3.5"

fork := true

scalacOptions ++= Seq(
  "-optimize", "-Yopt:l:classpath", "-target:jvm-1.8",
  "-Yinline", "-Yclosure-elim")

lwjgl.version := "2.9.3" // newer than in plugin

seq(lwjglSettings: _*)

