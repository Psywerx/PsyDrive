import de.johoop.findbugs4sbt._
import de.johoop.cpd4sbt.CopyPasteDetector._
import de.johoop.cpd4sbt.{ReportType => CPDReportType}
import org.scalastyle.sbt.{ScalastylePlugin,PluginKeys}

// Put these into your project/plugins.sbt
// addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.4.0")
// addSbtPlugin("de.johoop" % "findbugs4sbt" % "1.3.0")
// addSbtPlugin("de.johoop" % "cpd4sbt" % "1.1.4")
// addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.6")

scalacOptions ++= Seq(
  "-feature",
  //"-deprecation",
  "-unchecked",
  "-Xlint",
  //"-Xstrict-inference",
  "-Ywarn-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-inaccessible",
  "-Ywarn-infer-any",
  "-Ywarn-nullary-override",
  "-Ywarn-nullary-unit",
  //"-Ywarn-numeric-widen",
  "-Ywarn-value-discard")

// Linter
resolvers += "linter" at "http://hairyfotr.github.io/linteRepo/releases"

addCompilerPlugin("com.foursquare.lint" %% "linter" % "0.1-SNAPSHOT")

scalacOptions += "-P:linter:disable:UseHypot+CloseSourceFile"

// Scalastyle
ScalastylePlugin.Settings

PluginKeys.config <<= baseDirectory { base => base / "sca" / "scalastyle-config.xml" }

// Findbugs (optionally put findbugs plugins (such as fb-contrib and findsecbugs) jars into ~/.findbugs/plugin)
findbugsSettings

//findbugsEffort := Effort.Maximum

findbugsReportPath <<= baseDirectory { base => Some(base / "sca" / "findbugsoutput.xml") }

// CPD
cpdSettings

cpdTargetPath <<= baseDirectory { base => base / "sca" }

cpdReportName := "cpdoutput.txt"

cpdReportType := CPDReportType.Simple
