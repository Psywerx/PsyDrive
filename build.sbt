name := "PsyDrive"

organization := "Psywerx"

scalaVersion := "2.12.2"

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

libraryDependencies += "org.clojure" % "clojure" % "1.8.0"
libraryDependencies += "org.apache.commons" % "commons-math3" % "3.6.1"

fork := true

scalacOptions += "-opt:l:classpath"

lwjgl.version := "2.9.3" // newer than in plugin

seq(lwjglSettings: _*)

