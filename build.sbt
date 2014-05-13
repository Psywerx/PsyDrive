import de.johoop.findbugs4sbt._
import de.johoop.cpd4sbt.CopyPasteDetector._
import de.johoop.cpd4sbt.{ReportType => CPDReportType}
import org.scalastyle.sbt.ScalastylePlugin

scalaVersion := "2.11.0"

name := "PsyDrive"

organization := "Psywerx"

resolvers ++= Seq("snapshots", "releases").map(Resolver.sonatypeRepo)

scalacOptions ++= Seq(
  "-optimize",
  "-Yinline", "-Yclosure-elim",
  //"-feature",
  //"-deprecation",
  "-unchecked",
  //"-Xlint",
  //"-Xstrict-inference",
  "-Ywarn-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  //"-Ywarn-numeric-widen",
  "-Ywarn-value-discard")

seq(lwjglSettings: _*)

// Static analysis tools

// Linter
resolvers += "linter" at "http://hairyfotr.github.io/linteRepo/releases"

addCompilerPlugin("com.foursquare.lint" %% "linter" % "0.1-SNAPSHOT")

scalacOptions += "-P:linter:disable:UseHypot+CloseSourceFile"

// Scalastyle
ScalastylePlugin.Settings

// Findbugs (optionally put findbugs plugins (such as fb-contrib and findsecbugs) jars into ~/.findbugs/plugin)
findbugsSettings

//findbugsEffort := Effort.Maximum

findbugsReportPath <<= baseDirectory { base => Some(base / "reports" / "findbugsoutput.xml") }

// CPD
cpdSettings

cpdTargetPath <<= baseDirectory { base => base / "reports" }

cpdReportName := "cpdoutput.txt"

cpdReportType := CPDReportType.Simple

//cpdIgnoreLiterals := true

//cpdIgnoreIdentifiers := true

//cpdIgnoreAnnotations := true

//cpdSkipLexicalErrors := true
